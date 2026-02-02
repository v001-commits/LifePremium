package com.shyx.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 简单的Redis分布式锁实现类
 * 实现了ILock接口，提供了基于Redis的锁功能
 */
public class SimpleRedisLock implements ILock {

    private String name;  // 锁的名称
    private StringRedisTemplate stringRedisTemplate;  // Redis操作模板

    /**
     * 构造函数
     * @param stringRedisTemplate Redis操作模板
     * @param name 锁的名称
     */
    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    // 锁的前缀，用于在Redis中标识锁
    private static final String KEY_PREFIX = "lock:";
    // 唯一标识前缀，结合线程ID确保锁的唯一性
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    // 解锁的Lua脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 尝试获取锁
     * @param timeoutSec 锁的超时时间（秒）
     * @return 获取锁成功返回true，失败返回false
     */
    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程标识
        String threadId =ID_PREFIX + Thread.currentThread().getId();
        //获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unLock() {
        //调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }

    /*@Override
    public void unLock() {
        //获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        //判断标识是否一致
        if (threadId.equals(id)) {
            //释放锁
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }*/
}
