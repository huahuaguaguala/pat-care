package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String name;
    private Integer breedId;
    private String breedName;
    private BigDecimal age;
    /** 0=公 1=母 */
    private Integer gender;
    private BigDecimal weight;
    private String avatar;
    private String notes;
    private Integer popularity;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
