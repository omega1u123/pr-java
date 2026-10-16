package com.example.pillreminder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private boolean isCompleted;

    @OneToOne
    private ReminderEntity reminder;

    public ReminderDateEntity(Date date, boolean isCompleted, ReminderEntity reminder) {
        this.date = date;
        this.isCompleted = isCompleted;
        this.reminder = reminder;
    }
}
