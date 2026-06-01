package com.petcare.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("package_item")
public class PackageItem {
    @TableId(type = IdType.AUTO) private Long id;
    private Long packageId;
    private Long serviceId;
    private String serviceName;
    private Integer quantity;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
