package com.capstone.scheduler;

import com.capstone.scheduler.model.*;
import com.capstone.scheduler.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SchedulerApplication {

    private static final Logger log = LoggerFactory.getLogger(SchedulerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository,
                                         CategoryRepository categoryRepository,
                                         TaskRepository taskRepository,
                                         WorkflowRepository workflowRepository,
                                         PasswordEncoder passwordEncoder) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                if (userRepository.existsByUsername("admin")) {
                    log.info("Database already seeded. Skipping initialization.");
                    return;
                }

                log.info("Seeding database with test user, categories, tasks, and workflows...");

                User admin = User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.ROLE_USER)
                        .build();
                admin = userRepository.save(admin);

                Category work = Category.builder()
                        .name("Work")
                        .color("#4F46E5") 
                        .user(admin)
                        .build();
                Category personal = Category.builder()
                        .name("Personal")
                        .color("#EC4899") 
                        .user(admin)
                        .build();
                Category urgent = Category.builder()
                        .name("Urgent")
                        .color("#EF4444") 
                        .user(admin)
                        .build();

                categoryRepository.saveAll(List.of(work, personal, urgent));

                Task taskA = Task.builder()
                        .title("Setup Database Schema")
                        .description("Create primary user, task, categories, and workflow tables in the target database instance.")
                        .dueDate(LocalDateTime.now().plusDays(1))
                        .priority(Priority.HIGH)
                        .status(TaskStatus.PENDING)
                        .recurrenceType(RecurrenceType.NONE)
                        .category(work)
                        .user(admin)
                        .tags(new HashSet<>(Set.of("database", "sql")))
                        .build();
                taskA = taskRepository.saveAndFlush(taskA);

                Task taskB = Task.builder()
                        .title("Develop Backend REST Controllers")
                        .description("Build CRUD and utility endpoints for the scheduling and priority queue interfaces.")
                        .dueDate(LocalDateTime.now().plusDays(2))
                        .priority(Priority.MEDIUM)
                        .status(TaskStatus.PENDING)
                        .recurrenceType(RecurrenceType.NONE)
                        .category(work)
                        .user(admin)
                        .tags(new HashSet<>(Set.of("backend", "java")))
                        .dependencies(new HashSet<>(Set.of(taskA)))
                        .build();
                taskB = taskRepository.saveAndFlush(taskB);

                Task taskC = Task.builder()
                        .title("Wire Dashboard Frontend Views")
                        .description("Integrate Recharts, drag-and-drop Kanban, and the custom execution sorting dashboard elements.")
                        .dueDate(LocalDateTime.now().plusDays(4))
                        .priority(Priority.MEDIUM)
                        .status(TaskStatus.PENDING)
                        .recurrenceType(RecurrenceType.NONE)
                        .category(work)
                        .user(admin)
                        .tags(new HashSet<>(Set.of("frontend", "typescript")))
                        .dependencies(new HashSet<>(Set.of(taskB)))
                        .build();
                taskC = taskRepository.saveAndFlush(taskC);

                Task recurringTask = Task.builder()
                        .title("Daily Activity Audit Log")
                        .description("System maintenance audit job verifying queued task statuses.")
                        .dueDate(LocalDateTime.now().plusMinutes(5))
                        .priority(Priority.LOW)
                        .status(TaskStatus.PENDING)
                        .recurrenceType(RecurrenceType.DAILY)
                        .category(personal)
                        .user(admin)
                        .tags(new HashSet<>(Set.of("system", "logs")))
                        .build();
                taskRepository.saveAndFlush(recurringTask);

                Workflow wfArchiveHigh = Workflow.builder()
                        .name("Auto-Archive High Priority Completed Work Tasks")
                        .triggerEvent("TASK_COMPLETED")
                        .conditionField("PRIORITY")
                        .conditionValue("HIGH")
                        .actionType("AUTO_ARCHIVE")
                        .isActive(true)
                        .user(admin)
                        .build();

                Workflow wfAlertNotif = Workflow.builder()
                        .name("Create Notification Feed Trigger")
                        .triggerEvent("TASK_COMPLETED")
                        .conditionField("NONE")
                        .actionType("CREATE_NOTIFICATION")
                        .actionValue("System workflow notice: Target task completed successfully.")
                        .isActive(true)
                        .user(admin)
                        .build();

                workflowRepository.saveAll(List.of(wfArchiveHigh, wfAlertNotif));

                log.info("Database successfully seeded.");
            }
        };
    }
}
