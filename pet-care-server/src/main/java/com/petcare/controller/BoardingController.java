package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.BoardingRecord;
import com.petcare.mapper.BoardingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/boarding")
public class BoardingController {
    @Autowired private BoardingRecordMapper mapper;

    @PostMapping("/checkin")
    public Result<?> checkin(@RequestBody BoardingRecord record, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus(1);
        record.setCheckedInBy((Long) req.getAttribute("userId"));
        mapper.insert(record);
        return Result.success(record);
    }

    @PutMapping("/{id}/checkout")
    public Result<?> checkout(@PathVariable Long id, @RequestParam(required = false) String notes, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        BoardingRecord r = mapper.selectById(id);
        if (r == null || r.getStatus() != 1) return Result.fail("Invalid boarding record");
        r.setStatus(2);
        r.setCheckOutTime(LocalDateTime.now());
        r.setCheckedOutBy((Long) req.getAttribute("userId"));
        if (notes != null) r.setDailyNotes(notes);
        mapper.updateById(r);
        return Result.success(r);
    }

    @GetMapping("/active")
    public Result<?> active(HttpServletRequest req) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<BoardingRecord>().eq(BoardingRecord::getStatus, 1)));
    }

    @GetMapping("/pet/{petId}")
    public Result<?> petHistory(@PathVariable Long petId) {
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<BoardingRecord>()
                        .eq(BoardingRecord::getPetId, petId)
                        .orderByDesc(BoardingRecord::getCheckInTime)));
    }
}
