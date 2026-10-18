package com.example.pillreminder;

import com.example.pillreminder.entity.ReminderDateEntity;
import com.example.pillreminder.entity.ReminderEntity;
import com.example.pillreminder.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController()
@RequestMapping("api")
@RequiredArgsConstructor
public class Controller {
    private final ReminderRepository reminderRepository;
    private final ReminderDateRepository reminderDateRepository;
    private final UserRepository userRepository;

    @PostMapping("register")
    public ResponseEntity<Void> register(@RequestBody String id){
        if(id == null || id.isBlank())
            throw new IllegalArgumentException("id must not be empty");
        var user = new UserEntity(id);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("allByUserId/{userId}")
    public ResponseEntity<List<ReminderEntity>> findAllRemindersByUserId(@PathVariable("userId") String userId){
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        var reminders = reminderRepository.findAllByUser(user);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("allByUserId/{userId}/withDates")
    public ResponseEntity<List<ReminderDto>> findAllRemindersByUserIdWithDates(@PathVariable("userId") String userId){
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        var reminders = reminderRepository.findAllByUser(user);
        var reminderDtoList = new ArrayList<ReminderDto>();

        for(ReminderEntity r : reminders){
            var reminderDates = reminderDateRepository.findAllByReminder(r);
            reminderDtoList.add(new ReminderDto(r.getId(), r.getTitle(), r.getDosage(), r.getDescription(), reminderDates));
        }

        return ResponseEntity.ok(reminderDtoList);
    }

    @GetMapping("allByUserIdAndDate/{userId}/{date}")
    public ResponseEntity<List<ReminderDto>> findAllRemindersByUserIdAndDate(@PathVariable("userId") String userId,
                                                                             @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                             @RequestParam(value = "tz", defaultValue = "UTC") ZoneId zone){
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        var reminders = reminderRepository.findAllByUser(user);
        var reminderDtoList = new ArrayList<ReminderDto>();
        // Приёмы за весь день [00:00; 00:00 следующего дня) в часовом поясе клиента.
        var from = Date.from(date.atStartOfDay(zone).toInstant());
        var to = Date.from(date.plusDays(1).atStartOfDay(zone).toInstant());

        for(ReminderEntity r : reminders){
            var reminderDates = reminderDateRepository.findAllByReminderAndDateGreaterThanEqualAndDateLessThan(r, from, to);
            if(reminderDates.isEmpty())
                continue;
            reminderDtoList.add(new ReminderDto(r.getId(), r.getTitle(), r.getDosage(), r.getDescription(), reminderDates));
        }

        return ResponseEntity.ok(reminderDtoList);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ReminderDto> CreateReminder(@RequestBody CreateReminderDto dto){
        if(dto.userId() == null || dto.userId().isBlank())
            throw new IllegalArgumentException("userId must not be empty");
        if(dto.medicineName() == null || dto.medicineName().isBlank())
            throw new IllegalArgumentException("medicineName must not be empty");
        if(dto.dosage() == null || dto.dosage().isBlank())
            throw new IllegalArgumentException("dosage must not be empty");

        var user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + dto.userId()));
        var description = dto.description() == null ? "" : dto.description();
        var reminder = new ReminderEntity(dto.medicineName(), dto.dosage(), description, user);
        reminderRepository.save(reminder);
        List<ReminderDateEntity> reminderDates = new ArrayList<>();

        if(dto.dates() != null && !dto.dates().isEmpty()){
            for(Date d : dto.dates()){
                if(d == null)
                    throw new IllegalArgumentException("dates must not contain null");
                var reminderDate = new ReminderDateEntity(d, false, reminder);
                reminderDates.add(reminderDate);
            }
            reminderDateRepository.saveAll(reminderDates);
        }

        return ResponseEntity.ok(new ReminderDto(reminder.getId(), reminder.getTitle(), reminder.getDosage(), reminder.getDescription(), reminderDates));
    }

    @GetMapping("{reminderId}")
    public ResponseEntity<ReminderDto> findReminderById(@PathVariable("reminderId") UUID reminderId){
        var reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new NoSuchElementException("Reminder not found: " + reminderId));
        var reminderDates = reminderDateRepository.findAllByReminder(reminder);
        return ResponseEntity.ok(new ReminderDto(reminder.getId(), reminder.getTitle(), reminder.getDosage(), reminder.getDescription(), reminderDates));
    }

    @DeleteMapping("{reminderId}")
    @Transactional
    public ResponseEntity<Void> deleteReminderById(@PathVariable("reminderId") UUID reminderId){
        var reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new NoSuchElementException("Reminder not found: " + reminderId));
        var reminderDates = reminderDateRepository.findAllByReminder(reminder);
        reminderDateRepository.deleteAll(reminderDates);
        reminderRepository.delete(reminder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("updateReminderDate/{reminderDateId}")
    public ResponseEntity<ReminderDateEntity> updateReminderDateStatus(@PathVariable("reminderDateId") UUID reminderDateId, @RequestBody boolean status){
        var reminderDate = reminderDateRepository.findById(reminderDateId)
                .orElseThrow(() -> new NoSuchElementException("Reminder date not found: " + reminderDateId));
        reminderDate.setCompleted(status);
        reminderDateRepository.save(reminderDate);
        return ResponseEntity.ok(reminderDate);
    }

}
