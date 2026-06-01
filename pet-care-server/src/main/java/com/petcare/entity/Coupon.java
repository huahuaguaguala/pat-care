package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal threshold;
    private BigDecimal discount;
    private Integer totalStock;
    private Integer remainStock;
    private Integer perUserLimit;
    private String applicableServices;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
