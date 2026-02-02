package com.shyx.utils;

public interface ILock {

    /**
     * 尝试在指定的时间内获取锁，如果时间内获取成功则返回true，否则返回false
     *
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return 如果获取锁成功返回true，如果在指定时间内未获取到锁则返回false
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     * 该方法用于执行解锁操作，具体实现取决于业务场景
     */
    void unLock(); // 声明一个解锁方法，无返回值，无参数
}
