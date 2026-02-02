package com.shyx.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.shyx.dto.Result;
import com.shyx.entity.ShopType;
import com.shyx.mapper.ShopTypeMapper;
import com.shyx.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shyx.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate; // 自动注入StringRedisTemplate，用于操作Redis

    /**
     * 获取商户类型列表的方法，基于String实现
     * <p>
     * 首先尝试从Redis缓存中获取数据，如果缓存中没有则从数据库查询
     * 并将查询结果存入Redis缓存中，以提高后续查询效率
     * 本实现使用Redis的List数据结构存储商户类型信息，相比String类型更节省内存
     *
     * @return 返回Result对象，包含商户类型列表或错误信息
     */
    /*@Override
    public Result getTypeList() {
        //从redis中查找商户信息
        String key = "SHOP_TYPE_LIST";
        String shopType = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopType)) {
            //存在，直接返回
            List<ShopType> shopTypeList = JSONUtil.toList(shopType, ShopType.class);
            return Result.ok(shopTypeList);
        }
        //不存在，查询数据库
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //不存在，返回错误
        if (shopTypes.isEmpty()) {
            return Result.fail("没有查询到店铺类型");
        }
        //存在，存入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypes));
        return Result.ok(shopTypes);

    }*/

    /**
     * 获取商户类型列表的方法，基于List实现
     * <p>
     * 首先尝试从Redis缓存中获取数据，如果缓存中没有则从数据库查询
     * 并将查询结果存入Redis缓存中，以提高后续查询效率
     * 本实现使用Redis的List数据结构存储商户类型信息，相比String类型更节省内存
     */
    @Override
    public Result getTypeList() {
        //从redis中查找商户信息
        String key = CACHE_SHOP_TYPE_KEY;
        // 1. 从Redis查询 商铺类型缓存 , end:-1 表示取全部数据
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(key, 0, -1);
        // 2. 有就直接返回
        if (CollectionUtil.isNotEmpty(shopTypeJson)) {
            // JSON字符串转对象 排序后返回
            List<ShopType> shopTypes = JSONUtil.toList(shopTypeJson.toString(), ShopType.class);
            Collections.sort(shopTypes, ((o1, o2) -> o1.getSort() - o2.getSort()));
            return Result.ok(shopTypes);
        }
        // 3. 没有就查询数据库
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        // 4. 数据库没有就返回错误
        if (CollectionUtil.isEmpty(shopTypes)) {
            return Result.fail("没有查询到店铺类型");
        }
        // 5. 存在， 写入Redis，这里使用Redis的List类型。String类型，就是直接所有都写在一起，对内存开销比较大。
        // 要将List中的每个元素(元素类型ShopType) ，每个元素都要单独转成JSON，使用stream流的map映射
        // Hutools里的 BeanUtil.copyToList 本来想模仿UserService中的写法，
        // 传入一个CopyOptions的，但是setFieldValueEditor貌似只对beanToMap有效
        // 改用流的形式转换每个list元素
        List<String> shopTypesJson = shopTypes.stream().
                map(shopType -> JSONUtil.toJsonStr(shopType)).collect(Collectors.toList());
        // 因为从数据库读出来的时候已经是按照顺序读出来的，这里想要维持顺序必须从右边push，类似队列
        stringRedisTemplate.opsForList().rightPushAll(key, shopTypesJson);
        //设置其ttl
        stringRedisTemplate.expire(key, 30L, TimeUnit.MINUTES);
        // 5. 返回
        return Result.ok(shopTypes);
    }
}
