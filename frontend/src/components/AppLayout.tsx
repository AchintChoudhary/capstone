import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  CalendarDays,
  FileSpreadsheet,
  GitBranch,
  LayoutDashboard,
  LogOut,
  Menu,
  Moon,
  Search,
  Sun,
  Undo2,
  Redo2,
  Columns3,
  X,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import { notificationsApi, tasksApi } from '../api'
import TaskModal from './TaskModal'

const nav = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/kanban', label: 'Kanban', icon: Columns3 },
  { to: '/calendar', label: 'Calendar', icon: CalendarDays },
  { to: '/workflows', label: 'Workflows', icon: GitBranch },
  { to: '/notifications', label: 'Notifications', icon: Bell },
  { to: '/reports', label: 'Reports', icon: FileSpreadsheet },
]

export default function AppLayout() {
  const { user, logout } = useAuth()
  const { dark, toggle } = useTheme()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [unread, setUnread] = useState(0)
  const [showTaskModal, setShowTaskModal] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  useEffect(() => {
    notificationsApi.list().then((items) => {
      setUnread(items.filter((n) => !(n.isRead ?? n.read)).length)
    }).catch(() => undefined)
  }, [])

  useEffect(() => {
    if (!toast) return
    const t = setTimeout(() => setToast(null), 2800)
    return () => clearTimeout(t)
  }, [toast])

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault()
    navigate(search.trim() ? `/kanban?q=${encodeURIComponent(search.trim())}` : '/kanban')
    setOpen(false)
  }

  const runUndo = async () => {
    const res = await tasksApi.undo()
    setToast(res.message)
    window.dispatchEvent(new Event('tasks:refresh'))
  }

  const runRedo = async () => {
    const res = await tasksApi.redo()
    setToast(res.message)
    window.dispatchEvent(new Event('tasks:refresh'))
  }

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[240px_1fr]">
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-60 transform glass transition-transform lg:static lg:translate-x-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-full flex-col p-5">
          <div className="mb-8 flex items-center justify-between">
            <div>
              <p className="font-display text-2xl tracking-tight text-pine">FlowForge</p>
              <p className="text-xs text-ink/50 dark:text-mist/50">Smart Scheduler</p>
            </div>
            <button className="lg:hidden" onClick={() => setOpen(false)} aria-label="Close menu">
              <X size={18} />
            </button>
          </div>
          <nav className="flex flex-1 flex-col gap-1">
            {nav.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition ${
                    isActive
                      ? 'bg-pine text-white shadow-sm'
                      : 'text-ink/70 hover:bg-pine/10 dark:text-mist/70 dark:hover:bg-white/5'
                  }`
                }
              >
                <Icon size={18} />
                <span className="flex-1">{label}</span>
                {label === 'Notifications' && unread > 0 && (
                  <span className="rounded-full bg-coral px-2 py-0.5 text-[10px] font-semibold text-white">
                    {unread}
                  </span>
                )}
              </NavLink>
            ))}
          </nav>
          <button
            onClick={() => {
              logout()
              navigate('/login')
            }}
            className="mt-4 flex items-center gap-2 rounded-xl px-3 py-2.5 text-sm text-ink/60 hover:bg-coral/10 hover:text-coral dark:text-mist/60"
          >
            <LogOut size={16} /> Sign out
          </button>
        </div>
      </aside>

      {open && (
        <button
          className="fixed inset-0 z-30 bg-ink/30 lg:hidden"
          onClick={() => setOpen(false)}
          aria-label="Close overlay"
        />
      )}

      <div className="flex min-h-screen flex-col">
        <header className="sticky top-0 z-20 flex items-center gap-3 border-b border-ink/5 bg-foam/80 px-4 py-3 backdrop-blur dark:border-white/5 dark:bg-[#0b1413]/80">
          <button className="rounded-lg p-2 hover:bg-pine/10 lg:hidden" onClick={() => setOpen(true)}>
            <Menu size={20} />
          </button>
          <form onSubmit={onSearch} className="relative max-w-md flex-1">
            <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" size={16} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search tasks (Trie + KMP)…"
              className="w-full rounded-xl border border-ink/10 bg-white/70 py-2 pl-9 pr-3 text-sm outline-none ring-pine/30 focus:ring-2 dark:border-white/10 dark:bg-white/5"
            />
          </form>
          <div className="ml-auto flex items-center gap-1 sm:gap-2">
            <button onClick={runUndo} className="rounded-lg p-2 hover:bg-pine/10" title="Undo">
              <Undo2 size={18} />
            </button>
            <button onClick={runRedo} className="rounded-lg p-2 hover:bg-pine/10" title="Redo">
              <Redo2 size={18} />
            </button>
            <button
              onClick={() => setShowTaskModal(true)}
              className="hidden rounded-xl bg-pine px-3 py-2 text-sm font-medium text-white transition hover:bg-pine-deep sm:inline-flex"
            >
              New task
            </button>
            <button onClick={toggle} className="rounded-lg p-2 hover:bg-pine/10" aria-label="Toggle theme">
              {dark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium">{user?.username}</p>
              <p className="text-[11px] text-ink/45 dark:text-mist/45">{user?.role}</p>
            </div>
          </div>
        </header>

        <main className="flex-1 p-4 sm:p-6">
          <Outlet context={{ openTaskModal: () => setShowTaskModal(true) }} />
        </main>
      </div>

      {showTaskModal && (
        <TaskModal
          onClose={() => setShowTaskModal(false)}
          onSaved={() => {
            setShowTaskModal(false)
            window.dispatchEvent(new Event('tasks:refresh'))
            setToast('Task saved')
          }}
        />
      )}

      {toast && (
        <div className="fixed bottom-6 right-6 z-50 rounded-xl bg-ink px-4 py-3 text-sm text-white shadow-lg fade-in dark:bg-pine">
          {toast}
        </div>
      )}
    </div>
  )
}
