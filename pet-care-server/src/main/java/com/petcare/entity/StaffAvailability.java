package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate; import java.time.LocalDateTime; import java.time.LocalTime;
@Data @TableName("staff_availability")
public class StaffAvailability {
    @TableId(type = IdType.AUTO) private Long id;
    private Long staffId; private LocalDate slotDate;
    private LocalTime slotStart; private LocalTime slotEnd;
    private Integer isOpen; private String reason;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser; private Long updateUser;
}
