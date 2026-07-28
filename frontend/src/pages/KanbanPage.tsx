import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { tasksApi } from '../api'
import type { Task, TaskStatus } from '../types'
import TaskModal from '../components/TaskModal'
import { Archive, Check, Pencil, RotateCcw } from 'lucide-react'

const COLUMNS: { key: TaskStatus; label: string }[] = [
  { key: 'PENDING', label: 'Pending' },
  { key: 'IN_PROGRESS', label: 'In progress' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'ARCHIVED', label: 'Archived' },
]

const priorityTone: Record<string, string> = {
  HIGH: 'bg-coral/15 text-coral',
  MEDIUM: 'bg-amber/15 text-amber',
  LOW: 'bg-pine/15 text-pine',
}

export default function KanbanPage() {
  const [params] = useSearchParams()
  const query = params.get('q') ?? ''
  const [tasks, setTasks] = useState<Task[]>([])
  const [archived, setArchived] = useState<Task[]>([])
  const [sortBy, setSortBy] = useState('priority')
  const [editing, setEditing] = useState<Task | null>(null)
  const [dragId, setDragId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [active, arch] = await Promise.all([
        tasksApi.list({ query: query || undefined, sortBy }),
        tasksApi.archived(),
      ])
      setTasks(active)
      setArchived(arch)
    } finally {
      setLoading(false)
    }
  }, [query, sortBy])

  useEffect(() => {
    load()
    const onRefresh = () => load()
    window.addEventListener('tasks:refresh', onRefresh)
    return () => window.removeEventListener('tasks:refresh', onRefresh)
  }, [load])

  const board = useMemo(() => {
    const map: Record<string, Task[]> = {
      PENDING: [],
      IN_PROGRESS: [],
      COMPLETED: [],
      ARCHIVED: archived,
    }
    for (const t of tasks) {
      const status = (t.status || 'PENDING').toUpperCase()
      if (status === 'ARCHIVED') continue
      if (!map[status]) map[status] = []
      map[status].push(t)
    }
    return map
  }, [tasks, archived])

  const moveTask = async (task: Task, status: TaskStatus) => {
    if ((task.status || '').toUpperCase() === status) return
    if (status === 'COMPLETED') {
      await tasksApi.complete(task.id)
    } else if (status === 'ARCHIVED') {
      await tasksApi.archive(task.id)
    } else if ((task.status || '').toUpperCase() === 'ARCHIVED') {
      await tasksApi.restore(task.id)
      if (status !== 'PENDING') {
        await tasksApi.update(task.id, { ...task, status })
      }
    } else {
      await tasksApi.update(task.id, { ...task, status })
    }
    await load()
    window.dispatchEvent(new Event('tasks:refresh'))
  }

  const onDrop = async (status: TaskStatus) => {
    if (dragId == null) return
    const task = [...tasks, ...archived].find((t) => t.id === dragId)
    setDragId(null)
    if (!task) return
    await moveTask(task, status)
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-display text-3xl tracking-tight">Kanban</h1>
          <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
            Drag cards across columns{query ? ` · filtered by “${query}”` : ''}.
          </p>
        </div>
        <label className="text-sm">
          Sort by{' '}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="ml-1 rounded-lg border border-ink/10 bg-white/80 px-2 py-1.5 dark:border-white/10 dark:bg-white/5"
          >
            <option value="priority">Priority (merge sort)</option>
            <option value="dueDate">Due date (quick sort)</option>
            <option value="title">Title</option>
          </select>
        </label>
      </div>

      {loading ? (
        <p className="text-sm text-ink/50">Loading board…</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {COLUMNS.map((col) => (
            <div
              key={col.key}
              onDragOver={(e) => e.preventDefault()}
              onDrop={() => onDrop(col.key)}
              className="glass min-h-[420px] rounded-2xl p-3"
            >
              <div className="mb-3 flex items-center justify-between px-1">
                <h2 className="text-sm font-medium">{col.label}</h2>
                <span className="rounded-full bg-ink/5 px-2 py-0.5 text-xs dark:bg-white/10">
                  {board[col.key]?.length ?? 0}
                </span>
              </div>
              <div className="space-y-2">
                {(board[col.key] ?? []).map((task) => (
                  <article
                    key={task.id}
                    draggable
                    onDragStart={() => setDragId(task.id)}
                    className="cursor-grab rounded-xl border border-ink/5 bg-white/80 p-3 active:cursor-grabbing dark:border-white/5 dark:bg-white/5"
                  >
                    <div className="mb-2 flex items-start justify-between gap-2">
                      <h3 className="text-sm font-medium leading-snug">{task.title}</h3>
                      <span
                        className={`shrink-0 rounded-md px-1.5 py-0.5 text-[10px] font-semibold uppercase ${
                          priorityTone[task.priority] ?? priorityTone.MEDIUM
                        }`}
                      >
                        {task.priority}
                      </span>
                    </div>
                    {task.categoryName && (
                      <p className="mb-2 flex items-center gap-1 text-[11px] text-ink/50">
                        <span
                          className="inline-block h-2 w-2 rounded-full"
                          style={{ background: task.categoryColor ?? '#1a6b5a' }}
                        />
                        {task.categoryName}
                      </p>
                    )}
                    {task.dueDate && (
                      <p className="mb-2 text-[11px] text-ink/45">
                        Due {new Date(task.dueDate).toLocaleString()}
                      </p>
                    )}
                    <div className="flex flex-wrap gap-1">
                      <button
                        onClick={() => setEditing(task)}
                        className="rounded-md p-1 hover:bg-pine/10"
                        title="Edit"
                      >
                        <Pencil size={14} />
                      </button>
                      {col.key !== 'COMPLETED' && (
                        <button
                          onClick={() => moveTask(task, 'COMPLETED')}
                          className="rounded-md p-1 hover:bg-pine/10"
                          title="Complete"
                        >
                          <Check size={14} />
                        </button>
                      )}
                      {col.key !== 'ARCHIVED' ? (
                        <button
                          onClick={() => moveTask(task, 'ARCHIVED')}
                          className="rounded-md p-1 hover:bg-pine/10"
                          title="Archive"
                        >
                          <Archive size={14} />
                        </button>
                      ) : (
                        <button
                          onClick={() => moveTask(task, 'PENDING')}
                          className="rounded-md p-1 hover:bg-pine/10"
                          title="Restore"
                        >
                          <RotateCcw size={14} />
                        </button>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {editing && (
        <TaskModal
          task={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null)
            load()
          }}
        />
      )}
    </div>
  )
}
