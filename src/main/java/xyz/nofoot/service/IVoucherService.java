package xyz.nofoot.service;

import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
