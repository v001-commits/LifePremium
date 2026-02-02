package com.shyx.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * RedisIdWorker类，用于通过Redis生成唯一ID
 * 该类利用时间戳和序列号组合的方式生成分布式ID
 */
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳，用于计算相对时间
     * 这里设置为2022-01-01 00:00:00的UTC时间戳
     */
    private static final long BEGIN_TIMEAMP = 1640995200L;
    /**
     * 序列号位数，用于控制序列号在ID中的位数
     * 设置为32位，可以支持每秒约42亿次的ID生成
     */
    private static final int COUNT_BITS = 32;
    // 注入StringRedisTemplate，用于操作Redis
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成唯一ID的方法
     *
     * @param keyPrefix 用于区分不同业务场景的前缀，确保不同业务生成的ID不重复
     * @return 返回生成的唯一ID，由时间戳和序列号组合而成
     */
    public long nextId(String keyPrefix) {
        // 1.生成时间戳部分
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC); // 将当前时间转换为UTC时间戳（秒）
        long timestamp = nowSecond - BEGIN_TIMEAMP; // 减去开始时间戳，得到相对时间
        // 2.生成序列号部分
        // 2.1 获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd")); // 格式化日期为"年:月:日"的字符串形式
        // 2.2 自增
        // 使用Redis的原子操作进行计数，key格式为"icr:前缀:日期"
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 3.拼接并返回
        // 将时间戳左移COUNT_BITS位，然后与序列号进行按位或操作，组合成最终的ID
        return timestamp << COUNT_BITS | count;

    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }
}
