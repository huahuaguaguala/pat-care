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
    private Integer orderType;
    private Long userId;
    private Long petId;
    private Long staffId;
    private Long storeId;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime appointmentTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
    private LocalDateTime refundTime;
    private String refundReason;
    private BigDecimal refundAmount;
    private Integer rating;
    private String review;
    private String cancelReason;
    private String rejectionReason;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
