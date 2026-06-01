package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate; import java.time.LocalDateTime;
@Data @TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private Long petId; private Integer type;
    private String title; private String content;
    private Integer isRead; private LocalDate expireDate;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
