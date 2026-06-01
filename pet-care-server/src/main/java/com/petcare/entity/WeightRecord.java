package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Data @TableName("weight_record")
public class WeightRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long petId; private Long orderId; private BigDecimal weight;
    private LocalDate recordDate; private Long recordedBy; private String notes;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser; private Long updateUser;
}
