import { useCallback, useEffect, useState } from 'react'
import { notificationsApi } from '../api'
import type { NotificationItem } from '../types'
import { Bell, CheckCheck } from 'lucide-react'

function isRead(n: NotificationItem) {
  return n.isRead ?? n.read ?? false
}

export default function NotificationsPage() {
  const [items, setItems] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setItems(await notificationsApi.list())
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const mark = async (id: number) => {
    await notificationsApi.markRead(id)
    await load()
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-3xl tracking-tight">Notifications</h1>
        <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
          In-app alerts from reminders and workflow actions.
        </p>
      </div>

      {loading ? (
        <p className="text-sm text-ink/50">Loading…</p>
      ) : (
        <div className="space-y-2">
          {items.map((n) => (
            <article
              key={n.id}
              className={`glass flex items-start gap-3 rounded-2xl p-4 ${
                isRead(n) ? 'opacity-70' : ''
              }`}
            >
              <div className="mt-0.5 rounded-lg bg-pine/10 p-2 text-pine">
                <Bell size={16} />
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="font-medium">{n.title}</h2>
                  <span className="rounded-md bg-ink/5 px-1.5 py-0.5 text-[10px] uppercase dark:bg-white/10">
                    {n.type}
                  </span>
                  <span className="text-[10px] uppercase text-ink/40">{n.channel}</span>
                </div>
                <p className="mt-1 text-sm text-ink/65 dark:text-mist/65">{n.message}</p>
                <p className="mt-1 text-xs text-ink/40">
                  {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                </p>
              </div>
              {!isRead(n) && (
                <button
                  onClick={() => mark(n.id)}
                  className="rounded-lg p-2 text-pine hover:bg-pine/10"
                  title="Mark as read"
                >
                  <CheckCheck size={16} />
                </button>
              )}
            </article>
          ))}
          {items.length === 0 && (
            <p className="text-sm text-ink/45">No notifications yet.</p>
          )}
        </div>
      )}
    </div>
  )
}
