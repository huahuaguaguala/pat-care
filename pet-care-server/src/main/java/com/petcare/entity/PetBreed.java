package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_breed")
public class PetBreed {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal avgWeight;
    private Integer avgLifespan;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
