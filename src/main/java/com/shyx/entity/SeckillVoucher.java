package com.shyx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀优惠券实体类
 * 使用了Lombok注解简化代码：
 *
 * @Data - 自动生成getter、setter、toString等方法
 * @EqualsAndHashCode(callSuper = false) - 生成equals和hashCode方法，不调用父类的方法
 * @Accessors(chain = true) - 支持链式调用
 * @TableName("tb_seckill_voucher") - 指定对应的数据库表名
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_seckill_voucher")
public class SeckillVoucher implements Serializable {

    // 序列化版本号，用于Java序列化
    private static final long serialVersionUID = 1L;

    /**
     * 关联的优惠券的id
     * 使用@TableId注解标记为主键，并指定类型为INPUT，表示由应用输入而非数据库生成
     */
    @TableId(value = "voucher_id", type = IdType.INPUT)
    private Long voucherId;

    /**
     * 库存
     * 表示该秒杀优惠券的剩余数量
     */
    private Integer stock;

    /**
     * 创建时间
     * 记录优惠券创建的具体时间
     */
    private LocalDateTime createTime;

    /**
     * 生效时间
     * 表示优惠券开始生效的时间点
     */
    private LocalDateTime beginTime;

    /**
     * 失效时间
     * 表示优惠券失效的时间点
     */
    private LocalDateTime endTime;

    /**
     * 更新时间
     * 记录优惠券最后一次更新的时间
     */
    private LocalDateTime updateTime;


}
