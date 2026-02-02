package com.shyx.utils;

/**
 * 正则表达式工具类
 * 提供常用的正则表达式常量，用于验证手机号、邮箱、密码和验证码等格式
 */
public abstract class RegexPatterns {
    /**
     * 手机号正则
     * 支持中国大陆的手机号码格式
     * 规则：以1开头，第二位为3、4、5、6、7、8、9中的某些数字，后面跟着9位数字
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    /**
     * 邮箱正则
     * 支持标准的电子邮件地址格式
     * 规则：由字母、数字、下划线、点、@符号组成，@符号后必须有域名
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     * 密码正则。4~32位的字母、数字、下划线
     */
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";
    /**
     * 验证码正则, 6位数字或字母
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}
