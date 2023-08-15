package xyz.nofoot;

import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.Voucher;
import xyz.nofoot.service.IShopService;
import xyz.nofoot.service.IVoucherService;
import xyz.nofoot.utils.CacheClient;
import xyz.nofoot.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.nofoot.utils.RedisConstants;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ErApplicationTests {

    @Resource
    CacheClient cacheClient;

    @Resource
    IShopService shopService;

    @Resource
    RedisIdWorker redisIdWorker;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    IVoucherService voucherService;

    @Test
    void testSaveShop() {
        Result result = shopService.queryById(1L);
        Object data = result.getData();
//        Shop shop = JSONUtil.toBean((JSONObject) data, Shop.class);

        cacheClient.setWithLogicExpire(RedisConstants.CACHE_SHOP_KEY + 1, data, RedisConstants.CACHE_SHOP_TTL, TimeUnit.SECONDS);
    }

    private static final ExecutorService es = Executors.newFixedThreadPool(10);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void voucherTest() {
        Voucher voucher = voucherService.getById(2);
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), "100");
    }


}
