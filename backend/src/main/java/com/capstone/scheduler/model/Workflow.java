package com.capstone.scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflows")
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "trigger_event", nullable = false)
    private String triggerEvent; 

    @Column(name = "condition_field")
    private String conditionField; 

    @Column(name = "condition_value")
    private String conditionValue; 

    @Column(name = "action_type", nullable = false)
    private String actionType; 

    @Column(name = "action_value")
    private String actionValue; 

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Workflow() {}

    public Workflow(Long id, String name, String triggerEvent, String conditionField, String conditionValue,
                    String actionType, String actionValue, boolean isActive, User user) {
        this.id = id;
        this.name = name;
        this.triggerEvent = triggerEvent;
        this.conditionField = conditionField;
        this.conditionValue = conditionValue;
        this.actionType = actionType;
        this.actionValue = actionValue;
        this.isActive = isActive;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getConditionField() { return conditionField; }
    public void setConditionField(String conditionField) { this.conditionField = conditionField; }

    public String getConditionValue() { return conditionValue; }
    public void setConditionValue(String conditionValue) { this.conditionValue = conditionValue; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionValue() { return actionValue; }
    public void setActionValue(String actionValue) { this.actionValue = actionValue; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public static WorkflowBuilder builder() {
        return new WorkflowBuilder();
    }

    public static class WorkflowBuilder {
        private Long id;
        private String name;
        private String triggerEvent;
        private String conditionField;
        private String conditionValue;
        private String actionType;
        private String actionValue;
        private boolean isActive;
        private User user;

        public WorkflowBuilder id(Long id) { this.id = id; return this; }
        public WorkflowBuilder name(String name) { this.name = name; return this; }
        public WorkflowBuilder triggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; return this; }
        public WorkflowBuilder conditionField(String conditionField) { this.conditionField = conditionField; return this; }
        public WorkflowBuilder conditionValue(String conditionValue) { this.conditionValue = conditionValue; return this; }
        public WorkflowBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public WorkflowBuilder actionValue(String actionValue) { this.actionValue = actionValue; return this; }
        public WorkflowBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public WorkflowBuilder user(User user) { this.user = user; return this; }

        public Workflow build() {
            return new Workflow(id, name, triggerEvent, conditionField, conditionValue, actionType, actionValue, isActive, user);
        }
    }
}
