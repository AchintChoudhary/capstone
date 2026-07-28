package com.capstone.scheduler.repository;

import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.model.TaskStatus;
import com.capstone.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'ARCHIVED'")
    List<Task> findActiveByUser(@Param("user") User user);
    
    List<Task> findByUserAndStatus(User user, TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate < :now AND t.status != 'COMPLETED' AND t.status != 'ARCHIVED'")
    List<Task> findOverdueByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.dueDate <= :time AND t.recurrenceType = 'NONE'")
    List<Task> findPendingTasksDueBefore(@Param("time") LocalDateTime time);
    
    @Query("SELECT t FROM Task t WHERE t.recurrenceType != 'NONE' AND t.status != 'ARCHIVED'")
    List<Task> findRecurringTasks();

    @Query("SELECT t FROM Task t JOIN t.dependencies d WHERE d.id = :taskId AND t.status = 'PENDING'")
    List<Task> findDependentTasks(@Param("taskId") Long taskId);
}
