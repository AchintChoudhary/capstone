package com.capstone.scheduler.repository;

import com.capstone.scheduler.model.User;
import com.capstone.scheduler.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByUser(User user);
    List<Workflow> findByUserAndIsActive(User user, boolean isActive);
    
    @Query("SELECT w FROM Workflow w WHERE w.isActive = true AND w.triggerEvent = :triggerEvent")
    List<Workflow> findActiveByTriggerEvent(@Param("triggerEvent") String triggerEvent);
}
