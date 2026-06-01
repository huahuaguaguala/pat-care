package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String storeNo;
    private Long ownerId;
    private String name;
    private Integer breedId;
    private String breedName;
    private LocalDate birthday;
    private Integer gender;
    private BigDecimal weight;
    private Integer isNeutered;
    private String chipId;
    private String personality;
    private String notes;
    private Integer popularity;
    private String avatar;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
