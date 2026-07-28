import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) return <Navigate to="/" replace />

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(username, email, password)
      navigate('/')
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Registration failed'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="glass slide-up w-full max-w-md rounded-3xl p-8 shadow-xl">
        <p className="font-display text-3xl text-pine">FlowForge</p>
        <h1 className="mt-2 text-xl font-medium">Create account</h1>
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
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
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
              minLength={6}
              className="rounded-xl border border-ink/10 bg-white/80 px-3 py-2.5 outline-none focus:ring-2 focus:ring-pine/30 dark:border-white/10 dark:bg-white/5"
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="mt-2 rounded-xl bg-pine py-2.5 text-sm font-medium text-white hover:bg-pine-deep disabled:opacity-60"
          >
            {loading ? 'Creating…' : 'Create account'}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-ink/55 dark:text-mist/55">
          Already have an account?{' '}
          <Link to="/login" className="text-pine hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
