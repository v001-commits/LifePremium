package com.shyx.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置类
 * 用于配置Mybatis-Plus的相关组件，如分页插件等
 */
@Configuration  // 标识该类为配置类，相当于XML配置文件
public class MybatisConfig {

    /**
     * 配置Mybatis-Plus的插件
     *
     * @return MybatisPlusInterceptor Mybatis-Plus插件拦截器
     */
    @Bean  // 将该方法的返回值对象交给Spring容器管理
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建MybatisPlusInterceptor拦截器对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，并指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 返回配置好的拦截器
        return interceptor;
    }
}
