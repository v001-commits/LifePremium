package com.shyx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shyx.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  优惠券数据访问接口，继承自基础Mapper接口
 * 提供优惠券相关的数据库操作方法
 * @author ASUS
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 根据店铺ID查询该店铺下的所有优惠券
     * @param shopId 店铺ID
     * @return 返回店铺下的优惠券列表
     */
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
