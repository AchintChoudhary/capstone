import { useCallback, useEffect, useMemo, useState } from 'react'
import { tasksApi } from '../api'
import type { Task } from '../types'
import TaskModal from '../components/TaskModal'
import { ChevronLeft, ChevronRight } from 'lucide-react'

function startOfMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth(), 1)
}

function daysInMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate()
}

export default function CalendarPage() {
  const [cursor, setCursor] = useState(() => startOfMonth(new Date()))
  const [tasks, setTasks] = useState<Task[]>([])
  const [selected, setSelected] = useState<Task | null>(null)

  const load = useCallback(async () => {
    setTasks(await tasksApi.list())
  }, [])

  useEffect(() => {
    load()
    const onRefresh = () => load()
    window.addEventListener('tasks:refresh', onRefresh)
    return () => window.removeEventListener('tasks:refresh', onRefresh)
  }, [load])

  const byDay = useMemo(() => {
    const map = new Map<string, Task[]>()
    for (const t of tasks) {
      if (!t.dueDate) continue
      const key = t.dueDate.slice(0, 10)
      if (!map.has(key)) map.set(key, [])
      map.get(key)!.push(t)
    }
    return map
  }, [tasks])

  const year = cursor.getFullYear()
  const month = cursor.getMonth()
  const firstDow = startOfMonth(cursor).getDay()
  const total = daysInMonth(cursor)
  const cells: Array<number | null> = [
    ...Array.from({ length: firstDow }, () => null),
    ...Array.from({ length: total }, (_, i) => i + 1),
  ]

  const monthLabel = cursor.toLocaleString(undefined, { month: 'long', year: 'numeric' })

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-3xl tracking-tight">Calendar</h1>
          <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">Tasks plotted by due date.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            className="rounded-lg p-2 hover:bg-pine/10"
            onClick={() => setCursor(new Date(year, month - 1, 1))}
          >
            <ChevronLeft size={18} />
          </button>
          <span className="min-w-40 text-center text-sm font-medium">{monthLabel}</span>
          <button
            className="rounded-lg p-2 hover:bg-pine/10"
            onClick={() => setCursor(new Date(year, month + 1, 1))}
          >
            <ChevronRight size={18} />
          </button>
        </div>
      </div>

      <div className="glass overflow-hidden rounded-2xl">
        <div className="grid grid-cols-7 border-b border-ink/5 text-center text-xs uppercase tracking-wide text-ink/40 dark:border-white/5">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
            <div key={d} className="px-2 py-3">
              {d}
            </div>
          ))}
        </div>
        <div className="grid grid-cols-7 auto-rows-[minmax(100px,1fr)]">
          {cells.map((day, idx) => {
            if (day == null) {
              return <div key={`e-${idx}`} className="border-b border-r border-ink/5 bg-ink/[0.02] dark:border-white/5" />
            }
            const key = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
            const dayTasks = byDay.get(key) ?? []
            const isToday =
              key === new Date().toISOString().slice(0, 10)
            return (
              <div
                key={key}
                className={`min-h-24 border-b border-r border-ink/5 p-2 dark:border-white/5 ${
                  isToday ? 'bg-pine/5' : ''
                }`}
              >
                <p className={`mb-1 text-xs font-medium ${isToday ? 'text-pine' : 'text-ink/50'}`}>{day}</p>
                <div className="space-y-1">
                  {dayTasks.slice(0, 3).map((t) => (
                    <button
                      key={t.id}
                      onClick={() => setSelected(t)}
                      className="block w-full truncate rounded-md bg-pine/10 px-1.5 py-0.5 text-left text-[11px] text-pine-deep hover:bg-pine/20 dark:text-mist"
                      title={t.title}
                    >
                      {t.title}
                    </button>
                  ))}
                  {dayTasks.length > 3 && (
                    <p className="text-[10px] text-ink/40">+{dayTasks.length - 3} more</p>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </div>

      {selected && (
        <TaskModal
          task={selected}
          onClose={() => setSelected(null)}
          onSaved={() => {
            setSelected(null)
            load()
          }}
        />
      )}
    </div>
  )
}
