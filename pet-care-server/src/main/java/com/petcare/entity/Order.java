package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long serviceId;
    private Long staffId;
    private Long storeId;
    private BigDecimal amount;
    /** 0=待支付 1=已支付 2=服务中 3=已完成 4=已取消 */
    private Integer status;
    private LocalDateTime appointmentTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
    private Integer rating;
    private String review;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
