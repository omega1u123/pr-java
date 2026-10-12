package com.example.pillreminder;

import java.util.Date;
import java.util.List;

public record CreateReminderDto(
        String userId,
        String medicineName,
        String dosage,
        String description,
        List<Date> dates
) {
}
