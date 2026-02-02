package com.shyx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券表(Voucher)实体类
 * 该类用于表示优惠券信息，包含优惠券的基本属性和状态信息
 */
@Data // 使用Lombok的@Data注解，自动生成getter、setter等方法
@EqualsAndHashCode(callSuper = false) // 使用Lombok的@EqualsAndHashCode注解，不包含父类字段
@Accessors(chain = true) // 使用Lombok的@Accessors注解，开启链式调用
@TableName("tb_voucher") // MyBatis-Plus注解，指定对应的数据库表名为tb_voucher
public class Voucher implements Serializable { // 实现Serializable接口，支持序列化

    // 序列化版本号ID，用于版本控制
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     * 自增类型主键，对应数据库表中的id字段
     */
    @TableId(value = "id", type = IdType.AUTO) // MyBatis-Plus注解，定义主键字段为自增类型
    private Long id;

    /**
     * 商铺id
     * 关联商铺表的主键，用于标识优惠券所属的商铺
     */
    private Long shopId;

    /**
     * 代金券标题
     * 优惠券的显示标题，用于用户识别
     */
    private String title;

    /**
     * 副标题
     * 优惠券的补充说明信息
     */
    private String subTitle;

    /**
     * 使用规则
     * 优惠券的使用条件和限制说明
     */
    private String rules;

    /**
     * 支付金额
     * 使用优惠券需要支付的金额，单位为分
     */
    private Long payValue;

    /**
     * 抵扣金额
     * 优惠券可以抵扣的金额，单位为分
     */
    private Long actualValue;

    /**
     * 优惠券类型
     * 用于区分不同类型的优惠券（如满减券、折扣券等）
     */
    private Integer type;

    /**
     * 优惠券类型
     */
    private Integer status;
    /**
     * 库存
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * 生效时间
     */
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
