package xyz.nofoot.service.impl;

import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.Shop;
import xyz.nofoot.mapper.ShopMapper;
import xyz.nofoot.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.nofoot.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.nofoot.utils.RedisConstants;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 缓存空值， 解决缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
//        Shop shop = cacheClient.queryWithLogicExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("店铺不存在");
        }

        return Result.ok(shop);

    }

    @Override
    public Result updateShop(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            return Result.fail("店铺 ID 不存在");
        }

        updateById(shop);

        String key = RedisConstants.CACHE_SHOP_KEY + shopId;
        stringRedisTemplate.delete(key);

        return Result.ok();
    }
}
