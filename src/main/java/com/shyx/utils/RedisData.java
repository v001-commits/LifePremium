package com.shyx.utils;

import lombok.Data;

import java.time.LocalDateTime;

import lombok.Data;
import java.time.LocalDateTime;
/**
 * RedisData类用于封装Redis中存储的数据及其过期时间
 * 该类使用Lombok的@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class RedisData {
    /**
     * 过期时间，使用LocalDateTime类型表示精确到纳秒的时间
     * 用于标识该数据在Redis中的过期时间点
     */
    private LocalDateTime expireTime;
    /**
     * 实际存储的数据，使用Object类型以支持多种数据类型
     * 可以是字符串、对象、集合等各种类型的数据
     */
    private Object data;
}
