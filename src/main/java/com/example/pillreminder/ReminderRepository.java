package com.example.pillreminder;

import com.example.pillreminder.entity.ReminderEntity;
import com.example.pillreminder.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<ReminderEntity, UUID> {
    List<ReminderEntity> findAllByUser(UserEntity user);
}
