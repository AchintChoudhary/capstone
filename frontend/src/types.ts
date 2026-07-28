export type Priority = 'HIGH' | 'MEDIUM' | 'LOW'
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED'
export type RecurrenceType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CRON'

export interface AuthResponse {
  token: string
  username: string
  email: string
  role: string
}

export interface Task {
  id: number
  title: string
  description?: string
  dueDate?: string
  priority: Priority | string
  status: TaskStatus | string
  recurrenceType?: RecurrenceType | string
  cronExpression?: string
  categoryId?: number | null
  categoryName?: string | null
  categoryColor?: string | null
  tags?: string[]
  dependencyIds?: number[]
}

export interface Category {
  id: number
  name: string
  color: string
}

export interface Workflow {
  id: number
  name: string
  triggerEvent: string
  conditionField: string
  conditionValue: string
  actionType: string
  actionValue: string
  active?: boolean
  isActive?: boolean
}

export interface NotificationItem {
  id: number
  title: string
  message: string
  type: string
  channel: string
  read?: boolean
  isRead?: boolean
  createdAt?: string
}

export interface ActivityLog {
  id: number
  eventType: string
  message: string
  createdAt?: string
}

export interface Analytics {
  totalTasks: number
  completedTasks: number
  pendingTasks: number
  inProgressTasks: number
  overdueTasks: number
  priorityDistribution: Record<string, number>
  weeklyProductivity: Array<{
    day: string
    completed: number
    rollingAverage: number
  }>
  completionRate: number
}
