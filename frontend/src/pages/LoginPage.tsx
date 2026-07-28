import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('password')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) return <Navigate to="/" replace />

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate('/')
    } catch {
      setError('Invalid username or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="glass slide-up w-full max-w-md rounded-3xl p-8 shadow-xl">
        <p className="font-display text-3xl text-pine">FlowForge</p>
        <h1 className="mt-2 text-xl font-medium">Welcome back</h1>
        <p className="mt-1 text-sm text-ink/55 dark:text-mist/55">
          Sign in to your smart scheduler workspace.
        </p>
        <form onSubmit={submit} className="mt-6 grid gap-3">
          {error && <p className="rounded-lg bg-coral/10 px-3 py-2 text-sm text-coral">{error}</p>}
          <label className="grid gap-1 text-sm">
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2.5 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <label className="grid gap-1 text-sm">
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2.5 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="mt-2 rounded-xl bg-pine py-2.5 text-sm font-medium text-white hover:bg-pine-deep disabled:opacity-60"
          >
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-ink/55 dark:text-mist/55">
          No account?{' '}
          <Link to="/register" className="text-pine hover:underline">
            Register
          </Link>
        </p>
        <p className="mt-3 text-center text-xs text-ink/40">Demo: admin / password</p>
      </div>
    </div>
  )
}
