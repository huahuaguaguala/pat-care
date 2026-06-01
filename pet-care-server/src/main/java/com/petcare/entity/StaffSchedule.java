package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Data @TableName("staff_schedule")
public class StaffSchedule {
    @TableId(type = IdType.AUTO) private Long id;
    private Long staffId; private LocalDate workDate;
    private LocalTime startTime; private LocalTime endTime;
    private Integer maxSlots; private Integer status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser; private Long updateUser;
}
