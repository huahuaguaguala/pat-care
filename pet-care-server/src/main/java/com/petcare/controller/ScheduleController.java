package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.StaffSchedule;
import com.petcare.mapper.StaffScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired private StaffScheduleMapper scheduleMapper;

    @GetMapping("/staff")
    public Result<?> mySchedule(HttpServletRequest req) {
        Long staffId = (Long) req.getAttribute("userId");
        List<StaffSchedule> list = scheduleMapper.selectList(
                new LambdaQueryWrapper<StaffSchedule>()
                        .eq(StaffSchedule::getStaffId, staffId)
                        .ge(StaffSchedule::getWorkDate, LocalDate.now())
                        .orderByAsc(StaffSchedule::getWorkDate, StaffSchedule::getStartTime));
        return Result.success(list);
    }

    @GetMapping("/staff/{staffId}")
    public Result<?> staffSchedule(@PathVariable Long staffId, @RequestParam(required = false) String date) {
        LambdaQueryWrapper<StaffSchedule> qw = new LambdaQueryWrapper<StaffSchedule>()
                .eq(StaffSchedule::getStaffId, staffId);
        if (date != null) qw.eq(StaffSchedule::getWorkDate, LocalDate.parse(date));
        else qw.ge(StaffSchedule::getWorkDate, LocalDate.now());
        return Result.success(scheduleMapper.selectList(qw.orderByAsc(StaffSchedule::getWorkDate)));
    }

    @PostMapping
    public Result<?> add(@RequestBody StaffSchedule schedule, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        scheduleMapper.insert(schedule);
        return Result.success(schedule);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        scheduleMapper.deleteById(id);
        return Result.success();
    }
}
