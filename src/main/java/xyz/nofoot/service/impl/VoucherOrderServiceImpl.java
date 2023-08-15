package xyz.nofoot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.SeckillVoucher;
import xyz.nofoot.entity.VoucherOrder;
import xyz.nofoot.mapper.VoucherOrderMapper;
import xyz.nofoot.service.ISeckillVoucherService;
import xyz.nofoot.service.IVoucherOrderService;
import xyz.nofoot.utils.ILock;
import xyz.nofoot.utils.RedisIdWorker;
import xyz.nofoot.utils.SimpleRedisLock;
import xyz.nofoot.utils.UserHolder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    @Resource
//    private RedissonClient redissonClient;

    public static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true) {
                try {
                    // xgroup create stream.orders g1 0 mkstream
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    if (list == null || list.isEmpty()) {
                        continue;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);

                    handleVoucherOrder(voucherOrder);

                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }

            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    if (list == null || list.isEmpty()) {
                        break;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);

                    handleVoucherOrder(voucherOrder);

                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理 pending-list 异常", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        }

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            Long voucherId = voucherOrder.getVoucherId();
            Long userId = voucherOrder.getUserId();
//            RLock lock = redissonClient.getLock("order:" + userId);
            ILock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
            boolean isLock = lock.tryLock(10);
            if (!isLock) {
                log.error("不允许重复下单");
                return;
            }
            try {
                boolean isSuccess = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock", 0).update();
                if (!isSuccess) {
                    return;
                }
                save(voucherOrder);
            } finally {
                lock.unlock();
            }
        }
    }


    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        LocalDateTime beginTime = voucher.getBeginTime();
        LocalDateTime endTime = voucher.getEndTime();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(beginTime) || now.isAfter(endTime)) {
            return Result.fail("不在秒杀时间内");
        }

        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");

        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT
                , Collections.emptyList()
                , voucherId.toString()
                , userId.toString()
                , String.valueOf(orderId)
        );

        assert result != null;
        int r = result.intValue();

        if (r != 0) {
            String data = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
            stringRedisTemplate.opsForValue().increment("icr:" + "order:" + data, -1);
            return Result.fail(r == 1 ? "库存不足" : "您已经下过单");
        }

        return Result.ok(orderId);
    }
}
