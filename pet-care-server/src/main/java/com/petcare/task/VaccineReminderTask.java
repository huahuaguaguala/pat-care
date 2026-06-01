package com.petcare.task;

import com.petcare.entity.Notification;
import com.petcare.entity.Pet;
import com.petcare.entity.VaccinationRecord;
import com.petcare.mapper.NotificationMapper;
import com.petcare.mapper.PetMapper;
import com.petcare.mapper.VaccinationRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class VaccineReminderTask {
    private static final Logger log = LoggerFactory.getLogger(VaccineReminderTask.class);

    @Autowired private VaccinationRecordMapper vaccineMapper;
    @Autowired private PetMapper petMapper;
    @Autowired private NotificationMapper notificationMapper;

    /** Daily 6am: check vaccines due within 7 days, create notifications */
    @Scheduled(cron = "0 0 6 * * *")
    public void checkUpcomingVaccines() {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<VaccinationRecord> dueSoon = vaccineMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VaccinationRecord>()
                        .ge(VaccinationRecord::getNextDueDate, today)
                        .le(VaccinationRecord::getNextDueDate, in7Days));

        for (VaccinationRecord v : dueSoon) {
            Pet pet = petMapper.selectById(v.getPetId());
            if (pet == null) continue;

            // Check if notification already exists for this record
            Long exists = notificationMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
                            .eq(Notification::getPetId, v.getPetId())
                            .eq(Notification::getType, 0)
                            .eq(Notification::getExpireDate, v.getNextDueDate()));
            if (exists != null && exists > 0) continue;

            Notification n = new Notification();
            n.setUserId(pet.getOwnerId());
            n.setPetId(pet.getId());
            n.setType(0);
            n.setTitle(pet.getName() + " vaccine due");
            n.setContent(v.getVaccineName() + " is due on " + v.getNextDueDate());
            n.setExpireDate(v.getNextDueDate());
            notificationMapper.insert(n);

            log.info("Vaccine reminder created: pet={}, vaccine={}, due={}", pet.getName(), v.getVaccineName(), v.getNextDueDate());
        }
        log.info("Vaccine check done: {} reminders created", dueSoon.size());
    }
}
