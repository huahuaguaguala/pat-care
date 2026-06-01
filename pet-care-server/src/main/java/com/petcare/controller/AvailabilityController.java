package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.StaffAvailability;
import com.petcare.mapper.StaffAvailabilityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    @Autowired private StaffAvailabilityMapper mapper;

    @GetMapping("/my")
    public Result<?> mySlots(HttpServletRequest req) {
        Long staffId = (Long) req.getAttribute("userId");
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<StaffAvailability>()
                        .eq(StaffAvailability::getStaffId, staffId)
                        .ge(StaffAvailability::getSlotDate, LocalDate.now())));
    }

    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody StaffAvailability slot, HttpServletRequest req) {
        Long staffId = (Long) req.getAttribute("userId");
        slot.setStaffId(staffId);
        // Upsert: delete existing then insert
        mapper.delete(new LambdaQueryWrapper<StaffAvailability>()
                .eq(StaffAvailability::getStaffId, staffId)
                .eq(StaffAvailability::getSlotDate, slot.getSlotDate())
                .eq(StaffAvailability::getSlotStart, slot.getSlotStart()));
        mapper.insert(slot);
        return Result.success(slot);
    }
}
