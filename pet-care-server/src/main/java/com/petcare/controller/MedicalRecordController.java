package com.petcare.controller;
import com.petcare.common.Result;
import com.petcare.entity.MedicalRecord;
import com.petcare.mapper.MedicalRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/medical")
public class MedicalRecordController {
    @Autowired private MedicalRecordMapper mapper;

    @PostMapping
    public Result<?> add(@RequestBody MedicalRecord record, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        mapper.insert(record);
        return Result.success(record);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MedicalRecord record, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        record.setId(id);
        mapper.updateById(record);
        return Result.success(mapper.selectById(id));
    }

    @PutMapping("/{id}/resolve")
    public Result<?> resolve(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        MedicalRecord r = mapper.selectById(id);
        if (r == null) return Result.fail("Record not found");
        r.setIsResolved(1);
        mapper.updateById(r);
        return Result.success(r);
    }
}
