package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("boarding_record")
public class BoardingRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long petId; private Long orderId;
    private LocalDateTime checkInTime; private LocalDateTime checkOutTime;
    private LocalDateTime expectedCheckOut; private String cageNo;
    private String feedingInstructions; private Integer medicationNeeded;
    private String dailyNotes; private Integer status;
    private Long checkedInBy; private Long checkedOutBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser; private Long updateUser;
}
