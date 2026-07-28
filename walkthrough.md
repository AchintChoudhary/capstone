# Walkthrough — FlowForge Capstone Demo

Use this script for a live demo of the Smart Task Scheduler & Workflow Automation Engine.

## Setup (2 minutes)

1. Start backend: `cd backend` → `..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run`
2. Start frontend: `cd frontend` → `npm run dev`
3. Open http://localhost:5173
4. Sign in with **admin** / **password**

## Demo flow

### 1. Dashboard & analytics

- Show KPI cards (total, completed, in progress, overdue).
- Point out the **weekly productivity** line chart (sliding-window moving average).
- Show **priority distribution** pie chart and **status** bar chart.
- Scroll to **topological execution order** — dependency-safe Kahn sort of seeded tasks.
- Mention activity log panel (fills as you create/complete tasks).

### 2. Search (Trie + KMP)

- In the top search bar, type `database` or `Backend`.
- Submit to open Kanban filtered by the backend search service.

### 3. Kanban board

- Drag a **Pending** card to **In progress** (updates status via PUT).
- Mark a task complete with the check icon — triggers **WorkflowEngine**.
- Use **Sort by** → Priority (merge sort) / Due date (quick sort).
- Open **New task** from the header; set priority, category, tags, recurrence, and dependencies.
- Try adding a circular dependency — API rejects it with a clear error.

### 4. Undo / redo (Command pattern)

- Create or update a task.
- Click **Undo** in the top bar, then **Redo**.
- Refresh Kanban to confirm command-stack behavior.

### 5. Calendar

- Open **Calendar** and locate seeded tasks by due date.
- Click a task chip to edit it in the modal.

### 6. Workflows

- Open **Workflows** and explain Trigger → Condition → Action.
- Toggle a workflow off/on.
- Create a rule: `TASK_COMPLETED` + `PRIORITY=HIGH` → `CREATE_NOTIFICATION`.
- Complete a HIGH priority task and check **Notifications**.

### 7. Reports & logs

- Download CSV, JSON, Excel CSV, and text report.
- Show the activity log table (TASK_CREATE, TASK_UPDATE, WORKFLOW_EXEC, etc.).

### 8. Optional deep dives

- Swagger UI: http://localhost:8080/swagger-ui.html  
- H2 console: inspect `tasks`, `workflows`, `activity_logs`  
- `GET /api/tasks/topological` for raw execution order  

## Talking points (DSA & patterns)

| Feature shown | Implementation |
|---------------|----------------|
| Search | Trie indexing + KMP on descriptions |
| Dependency order | Custom DAG + Kahn topological sort + DFS cycle check |
| Sort dropdown | Custom MergeSort / QuickSort |
| Analytics trend | Sliding window moving average |
| Undo/redo | Command pattern + per-user stacks |
| Task creation | Fluent Builder on domain objects |
| Automation | WorkflowEngine observer on completion |
| Scheduling | SchedulerEngine + PriorityQueueManager |

## Expected seeded data

- User: `admin`  
- Categories: Work, Personal, Urgent  
- Tasks with a dependency chain (schema → controllers → frontend)  
- At least one daily recurring task and one sample workflow  
