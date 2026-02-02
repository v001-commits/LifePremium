package com.shyx;


import com.shyx.service.impl.ShopServiceImpl;
import com.shyx.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L, 10L);
    }

    @Resource
    private RedisIdWorker redisIdWorker;
    private ExecutorService es = Executors.newFixedThreadPool(500);

    /**
     * 测试ID生成器性能的测试方法
     * 使用多线程并发生成ID，并计算总耗时
     *
     * @throws InterruptedException 如果线程被中断
     */
    @Test
    void testIdWorker() throws InterruptedException {
        // 创建倒计数锁，用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(300);
        // 定义任务，每个线程生成100个ID
        Runnable task = () -> {
            // 循环生成100个ID
            for (int i = 0; i < 100; i++) {
                // 调用ID生成器生成ID
                long id = redisIdWorker.nextId("order");
                // 打印生成的ID
                System.out.println("id = " + id);
            }
            // 线程完成任务，计数器减1
            latch.countDown();
        };
        // 记录开始时间
        long begin = System.currentTimeMillis();
        // 提交300个任务到线程池
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        // 等待所有任务完成
        latch.await();
        // 记录结束时间
        long end = System.currentTimeMillis();
        // 打印总耗时
        System.out.println("time = " + (end - begin));
    }

}
