package com.example.pillreminder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "_user", nullable = false)
    private UserEntity user;


    public ReminderEntity(String title, String dosage, String description, UserEntity user) {
        this.title = title;
        this.dosage = dosage;
        this.description = description;
        this.user = user;
    }
}
