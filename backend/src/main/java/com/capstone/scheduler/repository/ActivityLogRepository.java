package com.capstone.scheduler.repository;

import com.capstone.scheduler.model.ActivityLog;
import com.capstone.scheduler.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT a FROM ActivityLog a WHERE a.user = :user OR a.user IS NULL ORDER BY a.createdAt DESC")
    List<ActivityLog> findUserAndSystemLogs(@Param("user") User user, Pageable pageable);
}
