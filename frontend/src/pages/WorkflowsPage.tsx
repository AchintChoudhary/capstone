import { useCallback, useEffect, useState } from 'react'
import { workflowsApi } from '../api'
import type { Workflow } from '../types'
import { ArrowRight, Plus, Power, Trash2 } from 'lucide-react'

const emptyForm = {
  name: '',
  triggerEvent: 'TASK_COMPLETED',
  conditionField: 'PRIORITY',
  conditionValue: 'HIGH',
  actionType: 'CREATE_NOTIFICATION',
  actionValue: 'High priority task completed',
}

function isActive(w: Workflow) {
  return w.isActive ?? w.active ?? false
}

export default function WorkflowsPage() {
  const [workflows, setWorkflows] = useState<Workflow[]>([])
  const [form, setForm] = useState(emptyForm)
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setWorkflows(await workflowsApi.list())
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const create = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await workflowsApi.create({ ...form, isActive: true })
      setForm(emptyForm)
      setShowForm(false)
      await load()
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Failed to create workflow'
      setError(msg)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-display text-3xl tracking-tight">Workflows</h1>
          <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
            Trigger → condition → action automation rules.
          </p>
        </div>
        <button
          onClick={() => setShowForm((s) => !s)}
          className="inline-flex items-center gap-2 rounded-xl bg-pine px-3 py-2 text-sm font-medium text-white hover:bg-pine-deep"
        >
          <Plus size={16} /> New workflow
        </button>
      </div>

      {showForm && (
        <form onSubmit={create} className="glass slide-up grid gap-3 rounded-2xl p-4 sm:grid-cols-2">
          {error && <p className="sm:col-span-2 rounded-lg bg-coral/10 px-3 py-2 text-sm text-coral">{error}</p>}
          <label className="grid gap-1 text-sm sm:col-span-2">
            Name
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <label className="grid gap-1 text-sm">
            Trigger
            <select
              value={form.triggerEvent}
              onChange={(e) => setForm({ ...form, triggerEvent: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            >
              <option value="TASK_COMPLETED">Task completed</option>
              <option value="TASK_OVERDUE">Task overdue</option>
              <option value="TASK_FAILED">Task failed</option>
            </select>
          </label>
          <label className="grid gap-1 text-sm">
            Condition field
            <select
              value={form.conditionField}
              onChange={(e) => setForm({ ...form, conditionField: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            >
              <option value="PRIORITY">Priority</option>
              <option value="CATEGORY">Category</option>
            </select>
          </label>
          <label className="grid gap-1 text-sm">
            Condition value
            <input
              required
              value={form.conditionValue}
              onChange={(e) => setForm({ ...form, conditionValue: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <label className="grid gap-1 text-sm">
            Action
            <select
              value={form.actionType}
              onChange={(e) => setForm({ ...form, actionType: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            >
              <option value="CREATE_NOTIFICATION">Create notification</option>
              <option value="SEND_EMAIL">Send email (mocked)</option>
              <option value="AUTO_ARCHIVE">Auto archive</option>
            </select>
          </label>
          <label className="grid gap-1 text-sm sm:col-span-2">
            Action value
            <input
              value={form.actionValue}
              onChange={(e) => setForm({ ...form, actionValue: e.target.value })}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <div className="sm:col-span-2">
            <button type="submit" className="rounded-xl bg-pine px-4 py-2 text-sm text-white hover:bg-pine-deep">
              Save workflow
            </button>
          </div>
        </form>
      )}

      <div className="grid gap-4">
        {workflows.map((w) => (
          <article key={w.id} className="glass slide-up rounded-2xl p-4">
            <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
              <div>
                <h2 className="font-medium">{w.name}</h2>
                <p className="text-xs text-ink/45">{isActive(w) ? 'Active' : 'Disabled'}</p>
              </div>
              <div className="flex gap-1">
                <button
                  onClick={async () => {
                    await workflowsApi.toggle(w.id)
                    load()
                  }}
                  className="rounded-lg p-2 hover:bg-pine/10"
                  title="Toggle"
                >
                  <Power size={16} className={isActive(w) ? 'text-pine' : 'text-ink/30'} />
                </button>
                <button
                  onClick={async () => {
                    await workflowsApi.remove(w.id)
                    load()
                  }}
                  className="rounded-lg p-2 hover:bg-coral/10 text-coral"
                  title="Delete"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <span className="rounded-xl bg-pine/10 px-3 py-2">{w.triggerEvent}</span>
              <ArrowRight size={14} className="text-ink/30" />
              <span className="rounded-xl bg-amber/10 px-3 py-2">
                {w.conditionField} = {w.conditionValue}
              </span>
              <ArrowRight size={14} className="text-ink/30" />
              <span className="rounded-xl bg-coral/10 px-3 py-2">
                {w.actionType}
                {w.actionValue ? `: ${w.actionValue}` : ''}
              </span>
            </div>
          </article>
        ))}
        {workflows.length === 0 && (
          <p className="text-sm text-ink/45">No workflows yet. Create one to automate task events.</p>
        )}
      </div>
    </div>
  )
}
