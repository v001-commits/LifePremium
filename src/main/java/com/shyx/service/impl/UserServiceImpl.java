package com.shyx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.dto.LoginFormDTO;
import com.shyx.dto.Result;
import com.shyx.dto.UserDTO;
import com.shyx.entity.User;
import com.shyx.mapper.UserMapper;
import com.shyx.service.IUserService;
import com.shyx.utils.RegexUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shyx.utils.RedisConstants.*;

/**
 * 用户服务实现类
 * 继承ServiceImpl<UserMapper, User>并实现IUserService接口
 * 提供用户相关的业务逻辑实现，包括发送验证码、登录、退出等功能
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * Redis模板对象，用于操作Redis数据库
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送短信验证码
     * 该方法用于向指定手机号发送短信验证码，并进行相关校验和存储
     *
     * @param phone   手机号，接收验证码的目标手机号码
     * @param session HttpSession对象，用于存储验证码（当前代码已改为使用Redis存储）
     * @return 返回操作结果，包含成功或失败信息
     * - 成功：返回Result.ok()
     * - 失败：返回Result.fail()并附带错误信息
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号格式是否正确，使用正则表达式验证手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合，返回错误信息，提示用户手机号格式错误
            return Result.fail("手机号格式错误！");
        }
        //如果符合，生成验证码
        String code = RandomUtil.randomNumbers(6); // 生成6位数字验证码
        //将验证码保存到session中（此代码已被注释，现改为使用Redis存储）
        //session.setAttribute("code", code);

        //将验证码保存到Redis中，并设置过期时间为2分钟
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES); // 使用Redis存储验证码

        //发送验证码到手机上
        log.debug("发送短信验证码成功，验证码：{}", code); // 记录日志，输出验证码（实际项目中应移除此行）
        //返回ok
        return Result.ok(); // 返回成功结果
    }

    /**
     * 用户登录方法
     * 处理用户登录逻辑，包括验证码校验、用户查询、用户信息存储等功能
     *
     * @param loginForm 登录表单数据传输对象，包含手机号和验证码
     * @param session   HTTP会话对象，用于存储用户登录状态
     * @return 返回操作结果，包含成功或失败信息
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //校验手机号格式是否正确
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        /*//从session中获取验证码并校验
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            //如果验证码错误，返回错误信息
            return Result.fail("验证码错误！");

        }*/
        //从Redis中获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone); // 从Redis获取缓存的验证码
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //如果验证码错误，返回错误信息
            return Result.fail("验证码错误！");
        }
        //一致，根据手机号查询用户
        User user = query().eq("phone", phone).one(); // 查询数据库中的用户信息
        //判断用户是否存在
        if (user == null) {
            //不存在，注册新用户并保存
            user = createUserWithPhone(phone); // 创建新用户
        }
        //保存用户信息到session中
        //随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //将user对象转换为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 将对象转为Map
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        /**
         *这样做难道不是只存储了值没有存储字段名吗？为什么会正确？
         实际上，字段名和字段值都会被正确存储，让我解释为什么：

         1. BeanUtil.beanToMap的工作机制：
         - fieldName和fieldValue是setFieldValueEditor回调方法的参数
         - fieldName用于标识当前处理的是哪个字段
         - fieldValue是该字段的值
         - BeanUtil会自动处理字段名到Map的key的映射

         2. setFieldValueEditor的作用：
         - 它只负责修改字段的值，不影响字段名的存储
         - lambda表达式只是告诉BeanUtil如何处理每个字段的值
         - 字段名会自动作为Map的key存储

         举个例子，如果UserDTO有这些字段：
         ```java
         UserDTO {
         id: 1001L,
         nickName: "张三"
         }
         ```

         转换过程：
         1. 处理id字段：
         - fieldName = "id"（自动作为Map的key）
         - fieldValue = 1001L（经过toString()变成"1001"）
         - 存储为：{"id": "1001"}

         2. 处理nickName字段：
         - fieldName = "nickName"（自动作为Map的key）
         - fieldValue = "张三"（经过toString()还是"张三"）
         - 存储为：{"nickName": "张三"}

         最终Map会包含：
         ```java
         {
         "id": "1001",
         "nickName": "张三"
         }
         ```

         所以不用担心，字段名会自动保留，我们只需要通过setFieldValueEditor处理字段值的转换即可。

         文件位置：[UserServiceImpl.java](src/main/java/com/shyx/service/impl/UserServiceImpl.java#L104-L107)
         */
        //设置token存储在redis中的形式
        String tokenKey = LOGIN_USER_KEY + token;
        // 将用户信息存储到Redis
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        //设置token的过期时间为2小时
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES); // 设置Redis中用户信息的过期时间
       /*
       //保存用户信息到session中
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        */ // 注释掉的Session存储方式
        return Result.ok(token); // 返回登录成功结果
    }

    @Override    // 表示重写父类的方法
    /**
     * 退出登录方法
     * @param session HttpSession对象，用于管理用户会话
     * @param request HttpServletRequest对象，用于获取请求头信息
     * @return 返回操作结果，表示退出是否成功
     */
    public Result logout(HttpSession session, HttpServletRequest request) {    // 定义退出登录方法，接收HttpSession参数
        //从session中移除用户
        session.removeAttribute("user"); // 从session中移除用户信息，清除登录状态
        session.invalidate(); // 使session失效，确保会话完全终止
        // 获取请求头中的token
        String tokenKey = LOGIN_USER_KEY + request.getHeader("authorization");
        // 判断redis中的tokenKey是否存在
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenKey))) {
            // 使redis中的token失效
            stringRedisTemplate.delete(tokenKey);
        }
        return Result.ok(); // 返回操作结果，表示退出成功
    }

    /**
     * 根据手机号创建新用户的方法
     * 当用户首次登录时，自动创建新用户并设置基本信息
     *
     * @param phone 手机号码
     * @return 创建好的用户对象
     */
    private User createUserWithPhone(String phone) {
        //创建用户对象实例
        User user = new User();
        //设置用户手机号
        user.setPhone(phone);
        //设置用户昵称为随机字符串，格式为"user_"加10位随机字符
        user.setNickName("user_" + RandomUtil.randomString(10)); // 生成随机昵称
        //保存用户信息到数据库
        save(user);
        //返回创建好的用户对象
        return user;

    }
}
