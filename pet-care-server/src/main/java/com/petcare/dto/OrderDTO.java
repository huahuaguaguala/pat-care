package com.petcare.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    @NotNull private Long petId;

    /** NEW: multiple service items per order */
    @NotNull private List<OrderItemDTO> items;

    private LocalDateTime appointmentTime;
    private String remark;
}
