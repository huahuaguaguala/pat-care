package com.petcare.controller;
import com.petcare.common.Result;
import com.petcare.entity.*;
import com.petcare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/health")
public class HealthRecordController {
    @Autowired private VaccinationRecordMapper vaccineMapper;
    @Autowired private MedicalRecordMapper medicalMapper;
    @Autowired private WeightRecordMapper weightMapper;

    /** Pet health timeline: vaccines + medical + weight combined */
    @GetMapping("/pet/{petId}/timeline")
    public Result<?> timeline(@PathVariable Long petId) {
        List<Map<String, Object>> events = new ArrayList<>();

        // Vaccines
        for (VaccinationRecord v : vaccineMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VaccinationRecord>()
                        .eq(VaccinationRecord::getPetId, petId)
                        .orderByDesc(VaccinationRecord::getAdministeredDate))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "vaccine");
            m.put("date", v.getAdministeredDate());
            m.put("title", v.getVaccineName() + " (Dose " + v.getDoseNumber() + ")");
            m.put("nextDue", v.getNextDueDate());
            m.put("detail", v);
            events.add(m);
        }

        // Medical records
        for (MedicalRecord mr : medicalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPetId, petId)
                        .orderByDesc(MedicalRecord::getVisitDate))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "medical");
            m.put("date", mr.getVisitDate());
            m.put("title", mr.getDiagnosis());
            m.put("resolved", mr.getIsResolved());
            m.put("detail", mr);
            events.add(m);
        }

        // Weight records
        for (WeightRecord w : weightMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getPetId, petId)
                        .orderByDesc(WeightRecord::getRecordDate))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "weight");
            m.put("date", w.getRecordDate());
            m.put("title", w.getWeight() + " kg");
            m.put("detail", w);
            events.add(m);
        }

        // Sort by date desc
        events.sort((a, b) -> {
            Comparable d1 = (Comparable) a.get("date");
            Comparable d2 = (Comparable) b.get("date");
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });

        return Result.success(events);
    }
}
