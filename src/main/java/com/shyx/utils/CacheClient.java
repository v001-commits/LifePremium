package com.shyx.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.shyx.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.shyx.utils.RedisConstants.*;

/**
 * 缓存客户端类，提供Redis缓存操作的相关功能
 * 使用StringRedisTemplate进行Redis操作，支持JSON序列化和反序列化
 */
@Slf4j
@Component
public class CacheClient {
    // Redis操作的模板类，用于执行Redis操作
    private StringRedisTemplate stringRedisTemplate;

    /**
     * CacheClient的构造函数，用于初始化StringRedisTemplate实例
     *
     * @param stringRedisTemplate Redis操作的模板类，用于执行Redis操作
     */
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        // 将传入的StringRedisTemplate实例赋值给类成员变量
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 设置键值对并指定过期时间
     *
     * @param key   键
     * @param value 值，将被转换为JSON字符串存储
     * @param time  过期时间
     * @param unit  时间单位
     */
    public void set(String key, Object value, long time, TimeUnit unit) {
        // 使用StringRedisTemplate的值操作API设置键值对
        // 将value对象转换为JSON字符串后存储
        // 同时设置指定的过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 设置带有逻辑过期时间的键值对
     *
     * @param key   Redis键
     * @param value 要存储的值
     * @param time  过期时间
     * @param unit  时间单位
     */
    public void setWithLogicalExpire(String key, Object value, long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 带缓存穿透防护的查询方法
     *
     * @param keyPrefix  Redis键前缀
     * @param id         数据ID
     * @param type       返回值类型
     * @param dbFallback 数据库查询函数
     * @param time       缓存过期时间
     * @param unit       时间单位
     * @return 查询结果
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id,
                                          Class<R> type, Function<ID, R> dbFallback,
                                          long time, TimeUnit unit) {
        String key = keyPrefix + id; // 定义Redis缓存键，拼接商铺ID
        //从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(json)) {
            //存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        //判断命中的是否是空值
        if (json != null) {
            //存在，直接返回错误信息
            return null;
        }
        //不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        //不存在，返回错误
        if (r == null) {
            //将空值写入redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //存在，将数据存入redis，设置缓存过期时间
        this.set(key, r, time, unit);
        //返回数据
        return r;
    }

}
