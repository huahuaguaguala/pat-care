package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pet_photo")
public class PetPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private String url;
    private Integer isPrimary;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
