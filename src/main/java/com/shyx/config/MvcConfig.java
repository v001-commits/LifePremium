package com.shyx.config;

import com.shyx.utils.LoginInterception;
import com.shyx.utils.RefreshTokenInterception;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类，用于配置Spring MVC的相关功能
 * 实现WebMvcConfigurer接口，重写其方法来定制化MVC配置
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

   /* @Resource
    private StringRedisTemplate stringRedisTemplate; // 注入StringRedisTemplate，用于操作Redis缓存*/

    @Autowired
    private LoginInterception loginInterception; // 注入登录拦截器，用于处理用户登录验证逻辑
   /* @Autowired
    private StringRedisTemplate stringRedisTemplate; // 注入StringRedisTemplate，用于操作Redis缓存*/
    @Autowired
    private RefreshTokenInterception refreshTokenInterception; // 注入刷新令牌拦截器，用于处理用户令牌刷新逻辑
    /**
     * 添加拦截器到注册表中的方法实现
     * 该方法是WebMvcConfigurer接口中的方法，用于配置Spring MVC的拦截器
     *
     * @param registry 拦截器注册表，用于注册和配置拦截器
     *                 通过这个注册表可以添加多个拦截器，并设置它们的拦截路径和排除路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加自定义的登录拦截器，并排除不需要拦截的路径
        // 排除了获取验证码的路径(/user/code)和用户登录的路径(/user/login)
        // 同时还排出了热门博客的路径(/blog/hot)，允许用户无需登录即可访问
        /*registry.addInterceptor(new LoginInterception(stringRedisTemplate)) // 创建登录拦截器实例，并注入StringRedisTemplate
                // 设置不需要拦截的路径集合
                .excludePathPatterns(
                        "/user/code",    // 获取验证码的接口，允许匿名访问
                        "/user/login",   // 用户登录接口，允许匿名访问
                        "blog/hot",       // 热门博客接口，允许匿名访问
                        "/shop/**",       // 商家详情接口，允许匿名访问
                        "/shop-type/**",  // 商家类型接口，允许匿名访问
                        "/upload/**",     // 文件上传接口，允许匿名访问
                        "/voucher/**"    // 优惠券接口，允许匿名访问
                );*/
        registry.addInterceptor(loginInterception) // 使用已注入的登录拦截器实例
                .excludePathPatterns( // 设置不需要拦截的路径集合
                        "/user/code",    // 获取验证码的接口，允许匿名访问
                        "/user/login",   // 用户登录接口，允许匿名访问
                        "blog/hot",       // 热门博客接口，允许匿名访问
                        "/shop/**",       // 商家详情接口，允许匿名访问
                        "/shop-type/**",  // 商家类型接口，允许匿名访问
                        "/upload/**",     // 文件上传接口，允许匿名访问
                        "/voucher/**"    // 优惠券接口，允许匿名访问
                ).order(1); // 设置拦截器的执行顺序为1，确保在刷新令牌拦截器之后执行
        // 添加刷新令牌拦截器，用于处理用户令牌刷新逻辑
//        registry.addInterceptor(new RefreshTokenInterception(stringRedisTemplate)).addPathPatterns("/**").order(0);
        registry.addInterceptor(refreshTokenInterception).addPathPatterns("/**").order(0);
    }
}
