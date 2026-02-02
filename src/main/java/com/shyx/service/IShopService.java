package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类接口 - ISHOPSERVICE
 * 该接口继承自MyBatis-Plus框架中的IService<Shop>接口，提供了店铺相关的业务操作方法
 * </p>
 *
 * @author 虎哥  接口的主要开发者
 * @since 2021-12-22  接口的创建日期
 */
public interface IShopService extends IService<Shop> {
    /**
     * 根据店铺ID查询店铺信息
     *
     * @param id 店铺的唯一标识ID
     * @return Result 返回查询结果，包含店铺信息或错误信息
     */
    Result queryById(Long id);

    /**
     * 更新店铺信息的方法
     *
     * @param shop 包含更新后店铺信息的对象
     * @return 返回操作结果，可能包含成功/失败状态及相关信息
     */
    Result update(Shop shop);
}
