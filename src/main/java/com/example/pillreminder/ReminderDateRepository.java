package com.example.pillreminder;

import com.example.pillreminder.entity.ReminderDateEntity;
import com.example.pillreminder.entity.ReminderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderDateRepository extends CrudRepository<ReminderDateEntity, UUID> {
    List<ReminderDateEntity> findAllByReminder(ReminderEntity reminder);
}
