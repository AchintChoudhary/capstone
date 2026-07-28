import { useEffect, useState } from 'react'
import { categoriesApi, tasksApi } from '../api'
import type { Category, Task } from '../types'
import { X } from 'lucide-react'

interface Props {
  task?: Task | null
  onClose: () => void
  onSaved: () => void
}

const empty = {
  title: '',
  description: '',
  dueDate: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  recurrenceType: 'NONE',
  cronExpression: '',
  categoryId: '' as string | number,
  tags: '',
  dependencyIds: [] as number[],
}

export default function TaskModal({ task, onClose, onSaved }: Props) {
  const [form, setForm] = useState(empty)
  const [categories, setCategories] = useState<Category[]>([])
  const [allTasks, setAllTasks] = useState<Task[]>([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([categoriesApi.list(), tasksApi.list()]).then(([cats, tasks]) => {
      setCategories(cats)
      setAllTasks(tasks)
    })
  }, [])

  useEffect(() => {
    if (!task) {
      setForm(empty)
      return
    }
    setForm({
      title: task.title ?? '',
      description: task.description ?? '',
      dueDate: task.dueDate ? task.dueDate.slice(0, 16) : '',
      priority: task.priority ?? 'MEDIUM',
      status: task.status ?? 'PENDING',
      recurrenceType: task.recurrenceType ?? 'NONE',
      cronExpression: task.cronExpression ?? '',
      categoryId: task.categoryId ?? '',
      tags: (task.tags ?? []).join(', '),
      dependencyIds: task.dependencyIds ?? [],
    })
  }, [task])

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload: Partial<Task> = {
        title: form.title.trim(),
        description: form.description.trim(),
        dueDate: form.dueDate ? new Date(form.dueDate).toISOString().slice(0, 19) : undefined,
        priority: form.priority,
        status: form.status,
        recurrenceType: form.recurrenceType,
        cronExpression: form.recurrenceType === 'CRON' ? form.cronExpression : undefined,
        categoryId: form.categoryId === '' ? null : Number(form.categoryId),
        tags: form.tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
        dependencyIds: form.dependencyIds,
      }
      if (task?.id) {
        await tasksApi.update(task.id, payload)
      } else {
        await tasksApi.create(payload)
      }
      onSaved()
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Could not save task'
      setError(msg)
    } finally {
      setSaving(false)
    }
  }

  const toggleDep = (id: number) => {
    setForm((f) => ({
      ...f,
      dependencyIds: f.dependencyIds.includes(id)
        ? f.dependencyIds.filter((d) => d !== id)
        : [...f.dependencyIds, id],
    }))
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-ink/40 p-4 sm:items-center">
      <form
        onSubmit={submit}
        className="glass slide-up max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl p-5 shadow-xl"
      >
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-display text-xl">{task ? 'Edit task' : 'Create task'}</h2>
          <button type="button" onClick={onClose} className="rounded-lg p-1 hover:bg-pine/10">
            <X size={18} />
          </button>
        </div>

        {error && <p className="mb-3 rounded-lg bg-coral/10 px-3 py-2 text-sm text-coral">{error}</p>}

        <div className="grid gap-3">
          <label className="grid gap-1 text-sm">
            Title
            <input
              required
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <label className="grid gap-1 text-sm">
            Description
            <textarea
              rows={3}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <div className="grid gap-3 sm:grid-cols-2">
            <label className="grid gap-1 text-sm">
              Due date
              <input
                type="datetime-local"
                value={form.dueDate}
                onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
                className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
              />
            </label>
            <label className="grid gap-1 text-sm">
              Priority
              <select
                value={form.priority}
                onChange={(e) => setForm({ ...form, priority: e.target.value })}
                className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
              >
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </label>
            <label className="grid gap-1 text-sm">
              Status
              <select
                value={form.status}
                onChange={(e) => setForm({ ...form, status: e.target.value })}
                className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
              >
                <option value="PENDING">Pending</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="COMPLETED">Completed</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </label>
            <label className="grid gap-1 text-sm">
              Category
              <select
                value={form.categoryId}
                onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
              >
                <option value="">None</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="grid gap-1 text-sm">
              Recurrence
              <select
                value={form.recurrenceType}
                onChange={(e) => setForm({ ...form, recurrenceType: e.target.value })}
                className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
              >
                <option value="NONE">None</option>
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
                <option value="CRON">Cron</option>
              </select>
            </label>
            {form.recurrenceType === 'CRON' && (
              <label className="grid gap-1 text-sm">
                Cron expression
                <input
                  value={form.cronExpression}
                  onChange={(e) => setForm({ ...form, cronExpression: e.target.value })}
                  placeholder="0 9 * * MON-FRI"
                  className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
                />
              </label>
            )}
          </div>
          <label className="grid gap-1 text-sm">
            Tags (comma-separated)
            <input
              value={form.tags}
              onChange={(e) => setForm({ ...form, tags: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <div className="grid gap-1 text-sm">
            <span>Dependencies</span>
            <div className="max-h-28 overflow-y-auto rounded-xl border border-ink/10 p-2 dark:border-white/10">
              {allTasks
                .filter((t) => t.id !== task?.id)
                .map((t) => (
                  <label key={t.id} className="flex items-center gap-2 py-1 text-xs">
                    <input
                      type="checkbox"
                      checked={form.dependencyIds.includes(t.id)}
                      onChange={() => toggleDep(t.id)}
                    />
                    {t.title}
                  </label>
                ))}
              {allTasks.filter((t) => t.id !== task?.id).length === 0 && (
                <p className="text-xs text-ink/40">No other tasks available</p>
              )}
            </div>
          </div>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl px-4 py-2 text-sm hover:bg-ink/5"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-pine px-4 py-2 text-sm font-medium text-white hover:bg-pine-deep disabled:opacity-60"
          >
            {saving ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}
