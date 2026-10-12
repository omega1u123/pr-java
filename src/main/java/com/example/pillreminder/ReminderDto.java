package com.example.pillreminder;

import com.example.pillreminder.entity.ReminderDateEntity;

import java.util.List;
import java.util.UUID;

public record ReminderDto(
        UUID id,
        String medicineName,
        String dosage,
        String description,
        List<ReminderDateEntity> dates
) {
}
