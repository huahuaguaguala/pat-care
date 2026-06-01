package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.WeightRecord;
import com.petcare.mapper.WeightRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/weight")
public class WeightController {
    @Autowired private WeightRecordMapper mapper;

    @GetMapping("/pet/{petId}")
    public Result<?> petWeightHistory(@PathVariable Long petId) {
        List<WeightRecord> records = mapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getPetId, petId)
                        .orderByAsc(WeightRecord::getRecordDate));
        return Result.success(records);
    }

    @PostMapping
    public Result<?> record(@RequestBody WeightRecord record, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        record.setRecordedBy((Long) req.getAttribute("userId"));
        record.setRecordDate(java.time.LocalDate.now());
        mapper.insert(record);
        return Result.success(record);
    }
}
