import { useCallback, useEffect, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { analyticsApi, logsApi, tasksApi } from '../api'
import type { ActivityLog, Analytics, Task } from '../types'
import { AlertTriangle, CheckCircle2, Clock3, ListTodo } from 'lucide-react'

const COLORS = ['#d4654a', '#c4922a', '#1a6b5a']

export default function DashboardPage() {
  const [metrics, setMetrics] = useState<Analytics | null>(null)
  const [logs, setLogs] = useState<ActivityLog[]>([])
  const [order, setOrder] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [a, l, t] = await Promise.all([
        analyticsApi.get(),
        logsApi.list(8),
        tasksApi.topological(),
      ])
      setMetrics(a)
      setLogs(l)
      setOrder(t.slice(0, 6))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
    const onRefresh = () => load()
    window.addEventListener('tasks:refresh', onRefresh)
    return () => window.removeEventListener('tasks:refresh', onRefresh)
  }, [load])

  if (loading && !metrics) {
    return <p className="text-sm text-ink/50">Loading dashboard…</p>
  }

  const priorityData = metrics
    ? Object.entries(metrics.priorityDistribution).map(([name, value]) => ({ name, value }))
    : []

  const kpis = [
    { label: 'Total tasks', value: metrics?.totalTasks ?? 0, icon: ListTodo, tone: 'text-pine' },
    { label: 'Completed', value: metrics?.completedTasks ?? 0, icon: CheckCircle2, tone: 'text-pine' },
    { label: 'In progress', value: metrics?.inProgressTasks ?? 0, icon: Clock3, tone: 'text-amber' },
    { label: 'Overdue', value: metrics?.overdueTasks ?? 0, icon: AlertTriangle, tone: 'text-coral' },
  ]

  return (
    <div className="space-y-6">
      <div className="slide-up">
        <h1 className="font-display text-3xl tracking-tight">Dashboard</h1>
        <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
          Productivity metrics, priority mix, and topological execution order.
        </p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {kpis.map((k, i) => (
          <div
            key={k.label}
            className={`glass slide-up stagger-${i + 1} rounded-2xl p-4`}
          >
            <div className="flex items-center justify-between">
              <p className="text-xs uppercase tracking-wide text-ink/45 dark:text-mist/45">{k.label}</p>
              <k.icon className={k.tone} size={18} />
            </div>
            <p className="mt-2 font-display text-3xl">{k.value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        <div className="glass slide-up rounded-2xl p-4 xl:col-span-2">
          <h2 className="mb-3 text-sm font-medium">Weekly productivity (sliding window)</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={metrics?.weeklyProductivity ?? []}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(15,28,26,0.08)" />
                <XAxis dataKey="day" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="completed" stroke="#1a6b5a" strokeWidth={2} />
                <Line type="monotone" dataKey="rollingAverage" stroke="#d4654a" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass slide-up rounded-2xl p-4">
          <h2 className="mb-3 text-sm font-medium">Priority distribution</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={priorityData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80}>
                  {priorityData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <p className="text-center text-sm text-ink/55">
            Completion rate: <strong>{metrics?.completionRate ?? 0}%</strong>
          </p>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="glass slide-up rounded-2xl p-4">
          <h2 className="mb-3 text-sm font-medium">Status breakdown</h2>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={[
                  { name: 'Pending', count: metrics?.pendingTasks ?? 0 },
                  { name: 'In progress', count: metrics?.inProgressTasks ?? 0 },
                  { name: 'Completed', count: metrics?.completedTasks ?? 0 },
                ]}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(15,28,26,0.08)" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#1a6b5a" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass slide-up rounded-2xl p-4">
          <h2 className="mb-3 text-sm font-medium">Suggested execution order (topological)</h2>
          <ul className="space-y-2">
            {order.map((t, i) => (
              <li key={t.id} className="flex items-center gap-3 rounded-xl bg-pine/5 px-3 py-2 text-sm">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-pine text-[11px] text-white">
                  {i + 1}
                </span>
                <span className="flex-1 truncate">{t.title}</span>
                <span className="text-[11px] uppercase text-ink/40">{t.priority}</span>
              </li>
            ))}
            {order.length === 0 && <p className="text-sm text-ink/40">No active tasks</p>}
          </ul>
        </div>
      </div>

      <div className="glass slide-up rounded-2xl p-4">
        <h2 className="mb-3 text-sm font-medium">Recent activity</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="text-xs uppercase text-ink/40">
              <tr>
                <th className="pb-2 pr-4">Event</th>
                <th className="pb-2 pr-4">Message</th>
                <th className="pb-2">When</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id} className="border-t border-ink/5 dark:border-white/5">
                  <td className="py-2 pr-4 font-medium text-pine">{log.eventType}</td>
                  <td className="py-2 pr-4">{log.message}</td>
                  <td className="py-2 text-ink/45">
                    {log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={3} className="py-4 text-ink/40">
                    No activity yet — complete a task to trigger workflow logs.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
