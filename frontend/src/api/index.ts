import api from './client'
import type {
  ActivityLog,
  Analytics,
  AuthResponse,
  Category,
  NotificationItem,
  Task,
  Workflow,
} from '../types'

export const authApi = {
  login: (username: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { username, password }).then((r) => r.data),
  register: (username: string, email: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { username, email, password }).then((r) => r.data),
}

export const tasksApi = {
  list: (params?: { query?: string; sortBy?: string }) =>
    api.get<Task[]>('/tasks', { params }).then((r) => r.data),
  archived: () => api.get<Task[]>('/tasks/archived').then((r) => r.data),
  create: (task: Partial<Task>) => api.post<Task>('/tasks', task).then((r) => r.data),
  update: (id: number, task: Partial<Task>) => api.put<Task>(`/tasks/${id}`, task).then((r) => r.data),
  remove: (id: number) => api.delete(`/tasks/${id}`),
  archive: (id: number) => api.post<Task>(`/tasks/${id}/archive`).then((r) => r.data),
  restore: (id: number) => api.post<Task>(`/tasks/${id}/restore`).then((r) => r.data),
  complete: (id: number) => api.post<Task>(`/tasks/${id}/complete`).then((r) => r.data),
  undo: () => api.post<{ success: boolean; message: string }>('/tasks/undo').then((r) => r.data),
  redo: () => api.post<{ success: boolean; message: string }>('/tasks/redo').then((r) => r.data),
  topological: () => api.get<Task[]>('/tasks/topological').then((r) => r.data),
}

export const categoriesApi = {
  list: () => api.get<Category[]>('/categories').then((r) => r.data),
  create: (data: { name: string; color: string }) =>
    api.post<Category>('/categories', data).then((r) => r.data),
  remove: (id: number) => api.delete(`/categories/${id}`),
}

export const workflowsApi = {
  list: () => api.get<Workflow[]>('/workflows').then((r) => r.data),
  create: (data: Partial<Workflow>) => api.post<Workflow>('/workflows', data).then((r) => r.data),
  update: (id: number, data: Partial<Workflow>) =>
    api.put<Workflow>(`/workflows/${id}`, data).then((r) => r.data),
  remove: (id: number) => api.delete(`/workflows/${id}`),
  toggle: (id: number) => api.post<Workflow>(`/workflows/${id}/toggle`).then((r) => r.data),
}

export const notificationsApi = {
  list: () => api.get<NotificationItem[]>('/notifications').then((r) => r.data),
  markRead: (id: number) => api.post(`/notifications/${id}/read`),
}

export const analyticsApi = {
  get: () => api.get<Analytics>('/analytics').then((r) => r.data),
}

export const logsApi = {
  list: (limit = 50) => api.get<ActivityLog[]>('/logs', { params: { limit } }).then((r) => r.data),
}

export async function exportReport(format: 'csv' | 'pdf' | 'excel' | 'json') {
  const response = await api.get('/reports/export', {
    params: { format },
    responseType: 'blob',
  })
  const disposition = response.headers['content-disposition'] as string | undefined
  const match = disposition?.match(/filename=([^;]+)/)
  const filename = match?.[1]?.trim() ?? `task_report.${format}`
  const url = URL.createObjectURL(response.data)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
