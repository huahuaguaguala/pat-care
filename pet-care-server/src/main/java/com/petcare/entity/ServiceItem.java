package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("service_item")
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private Integer maxPerSlot;
    private String image;
    private Integer status;
}
