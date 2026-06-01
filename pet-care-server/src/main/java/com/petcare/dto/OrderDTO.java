package com.petcare.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    @NotNull private Long petId;
    @NotNull private Long serviceId;
    private Long storeId;
    private LocalDateTime appointmentTime;
    private String remark;
}
