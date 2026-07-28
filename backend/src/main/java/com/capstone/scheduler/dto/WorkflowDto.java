package com.capstone.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowDto {
    private Long id;
    private String name;
    private String triggerEvent;
    private String conditionField;
    private String conditionValue;
    private String actionType;
    private String actionValue;
    @JsonProperty("isActive")
    private boolean isActive;

    public WorkflowDto() {}

    public WorkflowDto(Long id, String name, String triggerEvent, String conditionField, String conditionValue,
                       String actionType, String actionValue, boolean isActive) {
        this.id = id;
        this.name = name;
        this.triggerEvent = triggerEvent;
        this.conditionField = conditionField;
        this.conditionValue = conditionValue;
        this.actionType = actionType;
        this.actionValue = actionValue;
        this.isActive = isActive;
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

    public static WorkflowDtoBuilder builder() {
        return new WorkflowDtoBuilder();
    }

    public static class WorkflowDtoBuilder {
        private Long id;
        private String name;
        private String triggerEvent;
        private String conditionField;
        private String conditionValue;
        private String actionType;
        private String actionValue;
        private boolean isActive;

        public WorkflowDtoBuilder id(Long id) { this.id = id; return this; }
        public WorkflowDtoBuilder name(String name) { this.name = name; return this; }
        public WorkflowDtoBuilder triggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; return this; }
        public WorkflowDtoBuilder conditionField(String conditionField) { this.conditionField = conditionField; return this; }
        public WorkflowDtoBuilder conditionValue(String conditionValue) { this.conditionValue = conditionValue; return this; }
        public WorkflowDtoBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public WorkflowDtoBuilder actionValue(String actionValue) { this.actionValue = actionValue; return this; }
        public WorkflowDtoBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }

        public WorkflowDto build() {
            return new WorkflowDto(id, name, triggerEvent, conditionField, conditionValue, actionType, actionValue, isActive);
        }
    }
}
