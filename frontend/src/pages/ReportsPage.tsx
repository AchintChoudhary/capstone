import { useCallback, useEffect, useState } from 'react'
import { exportReport, logsApi } from '../api'
import type { ActivityLog } from '../types'
import { Download, FileJson, FileSpreadsheet, FileText } from 'lucide-react'

const formats = [
  { key: 'csv' as const, label: 'CSV', icon: FileSpreadsheet, desc: 'Comma-separated spreadsheet' },
  { key: 'excel' as const, label: 'Excel CSV', icon: FileSpreadsheet, desc: 'Excel-friendly export' },
  { key: 'json' as const, label: 'JSON', icon: FileJson, desc: 'Structured task payload' },
  { key: 'pdf' as const, label: 'Text report', icon: FileText, desc: 'Formatted mock PDF text' },
]

export default function ReportsPage() {
  const [logs, setLogs] = useState<ActivityLog[]>([])
  const [busy, setBusy] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLogs(await logsApi.list(100))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const download = async (format: 'csv' | 'pdf' | 'excel' | 'json') => {
    setBusy(format)
    try {
      await exportReport(format)
    } finally {
      setBusy(null)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-3xl tracking-tight">Reports & logs</h1>
        <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
          Export task data and review the activity audit trail.
        </p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {formats.map((f) => (
          <button
            key={f.key}
            onClick={() => download(f.key)}
            disabled={busy !== null}
            className="glass slide-up rounded-2xl p-4 text-left transition hover:bg-pine/5 disabled:opacity-60"
          >
            <f.icon className="text-pine" size={20} />
            <p className="mt-3 font-medium">{f.label}</p>
            <p className="mt-1 text-xs text-ink/50">{f.desc}</p>
            <p className="mt-3 inline-flex items-center gap-1 text-xs text-pine">
              <Download size={12} /> {busy === f.key ? 'Downloading…' : 'Download'}
            </p>
          </button>
        ))}
      </div>

      <div className="glass rounded-2xl p-4">
        <h2 className="mb-3 text-sm font-medium">Activity log</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="text-xs uppercase text-ink/40">
              <tr>
                <th className="pb-2 pr-4">ID</th>
                <th className="pb-2 pr-4">Event</th>
                <th className="pb-2 pr-4">Message</th>
                <th className="pb-2">Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id} className="border-t border-ink/5 dark:border-white/5">
                  <td className="py-2 pr-4 text-ink/40">{log.id}</td>
                  <td className="py-2 pr-4 font-medium text-pine">{log.eventType}</td>
                  <td className="py-2 pr-4">{log.message}</td>
                  <td className="py-2 whitespace-nowrap text-ink/45">
                    {log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={4} className="py-4 text-ink/40">
                    No logs recorded yet.
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
