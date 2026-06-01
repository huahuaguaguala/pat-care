package com.petcare.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class OrderItemDTO {
    @NotNull private Long serviceId;
    @NotNull private Integer quantity;
}
