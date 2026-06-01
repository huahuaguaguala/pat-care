package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long orderId;
    private LocalDate visitDate;
    private Integer recordType;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String medication;
    private BigDecimal temperature;
    private BigDecimal weight;
    private String vetName;
    private LocalDate followUpDate;
    private Integer isResolved;
    private String notes;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
