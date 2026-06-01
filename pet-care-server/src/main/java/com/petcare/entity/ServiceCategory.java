package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("service_category")
public class ServiceCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
}
