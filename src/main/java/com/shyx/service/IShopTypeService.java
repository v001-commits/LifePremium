package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 * 这是一个服务接口，继承自MyBatis-Plus的IService接口，用于处理ShopType相关的业务逻辑

 *
 * @author 虎哥    // 作者信息
 * @since 2021-12-22    // 创建日期
 */
public interface IShopTypeService extends IService<ShopType> {    // 定义服务接口，继承IService<ShopType>，泛型参数ShopType表示该服务处理的是ShopType实体类
    Result getTypeList();    // 定义一个show方法，返回Result类型的结果，用于展示店铺类型信息
}
