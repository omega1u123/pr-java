package com.example.pillreminder;

import com.example.pillreminder.entity.ReminderDateEntity;
import com.example.pillreminder.entity.ReminderEntity;
import com.example.pillreminder.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        var user = new UserEntity(id);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("allByUserId/{userId}")
    public ResponseEntity<List<ReminderEntity>> findAllRemindersByUserId(@PathVariable("userId") String userId){
        var user = userRepository.findById(userId).orElseThrow();
        var reminders = reminderRepository.findAllByUser(user);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("allByUserIdAndDate/{userId}/{date}")
    public ResponseEntity<List<ReminderDto>> findAllRemindersByUserIdAndDate(@PathVariable("userId") String userId, @PathVariable("date") Date date){
        var user = userRepository.findById(userId).orElseThrow();
        var reminders = reminderRepository.findAllByUser(user);
        var reminderDtoList = new ArrayList<ReminderDto>();

        for(ReminderEntity r : reminders){
            var reminderDates = reminderDateRepository.findAllByReminderAndDate(r, date);
            if(reminderDates.isEmpty())
                continue;
            reminderDtoList.add(new ReminderDto(r.getId(), r.getTitle(), r.getDosage(), r.getDescription(), reminderDates));
        }

        return ResponseEntity.ok(reminderDtoList);
    }

    @PostMapping
    public ResponseEntity<ReminderDto> CreateReminder(@RequestBody CreateReminderDto dto){
        var user = userRepository.findById(dto.userId()).orElseThrow();
        var reminder = new ReminderEntity(dto.medicineName(), dto.dosage(), dto.description(), user);
        reminderRepository.save(reminder);
        List<ReminderDateEntity> reminderDates = new ArrayList<>();

        if(!dto.dates().isEmpty()){
            for(Date d : dto.dates()){
                var reminderDate = new ReminderDateEntity(d, false, reminder);
                reminderDates.add(reminderDate);
            }
            reminderDateRepository.saveAll(reminderDates);
        }

        return ResponseEntity.ok(new ReminderDto(reminder.getId(), reminder.getTitle(), reminder.getDosage(), reminder.getDescription(), reminderDates));
    }

    @GetMapping("{reminderId:\\s}")
    public ResponseEntity<ReminderDto> findReminderById(@PathVariable("reminderId") UUID reminderId){
        var reminder = reminderRepository.findById(reminderId).orElseThrow();
        var reminderDates = reminderDateRepository.findAllByReminder(reminder);
        return ResponseEntity.ok(new ReminderDto(reminder.getId(), reminder.getTitle(), reminder.getDosage(), reminder.getDescription(), reminderDates));
    }

    @DeleteMapping("{reminderId:\\s}")
    public ResponseEntity<Void> deleteReminderById(@PathVariable("reminderId") UUID reminderId){
        var reminder = reminderRepository.findById(reminderId).orElseThrow();
        var reminderDates = reminderDateRepository.findAllByReminder(reminder);
        reminderDateRepository.deleteAll(reminderDates);
        reminderRepository.delete(reminder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("updateReminderDate/{reminderDateId:\\s}")
    public ResponseEntity<ReminderDateEntity> updateReminderDateStatus(@PathVariable("reminderDateId") UUID reminderDateId, @RequestBody boolean status){
        var reminderDate = reminderDateRepository.findById(reminderDateId).orElseThrow();
        reminderDate.setCompleted(status);
        reminderDateRepository.save(reminderDate);
        return ResponseEntity.ok(reminderDate);
    }

}
