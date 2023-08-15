package xyz.nofoot.service;

import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result updateShop(Shop shop);
}
