package com.petcare.controller;
import com.petcare.common.Result;
import com.petcare.entity.VaccinationRecord;
import com.petcare.mapper.VaccinationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vaccine")
public class VaccineController {
    @Autowired private VaccinationRecordMapper mapper;

    @PostMapping
    public Result<?> add(@RequestBody VaccinationRecord record, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        if (record.getAdministeredDate() == null) record.setAdministeredDate(LocalDate.now());
        mapper.insert(record);
        return Result.success(record);
    }

    @GetMapping("/pet/{petId}")
    public Result<?> petVaccines(@PathVariable Long petId) {
        return Result.success(mapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VaccinationRecord>()
                        .eq(VaccinationRecord::getPetId, petId)
                        .orderByDesc(VaccinationRecord::getAdministeredDate)));
    }

    @GetMapping("/due")
    public Result<?> upcomingDue(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        List<VaccinationRecord> list = mapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VaccinationRecord>()
                        .le(VaccinationRecord::getNextDueDate, LocalDate.now().plusDays(30))
                        .ge(VaccinationRecord::getNextDueDate, LocalDate.now())
                        .orderByAsc(VaccinationRecord::getNextDueDate));
        return Result.success(list);
    }
}
