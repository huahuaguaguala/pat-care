package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccination_record")
public class VaccinationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long orderId;
    private String vaccineName;
    private String vaccineType;
    private Integer doseNumber;
    private LocalDate administeredDate;
    private LocalDate nextDueDate;
    private Long administeredBy;
    private String clinicName;
    private String batchNumber;
    private String notes;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
