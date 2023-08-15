package xyz.nofoot.service.impl;

import xyz.nofoot.entity.ShopType;
import xyz.nofoot.mapper.ShopTypeMapper;
import xyz.nofoot.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

}
