import { useState } from 'react'
import './App.css'
import { supabase, isSupabaseConfigured } from './supabaseClient'

function App() {
  const [isLogin, setIsLogin] = useState(true)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [isError, setIsError] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!isSupabaseConfigured) {
      setIsError(true)
      setMessage('⚠️ Supabase not configured. Please add your credentials to .env.local')
      return
    }

    setLoading(true)
    setMessage('')
    setIsError(false)

    try {
      if (isLogin) {
        // Login
        const { error } = await supabase.auth.signInWithPassword({
          email,
          password,
        })

        if (error) {
          setIsError(true)
          setMessage(error.message)
        } else {
          setMessage('Login successful! Redirecting...')
          setTimeout(() => {
            window.location.href = '/dashboard'
          }, 1500)
        }
      } else {
        // Register
        if (password !== confirmPassword) {
          setIsError(true)
          setMessage('Passwords do not match')
          setLoading(false)
          return
        }

        if (password.length < 6) {
          setIsError(true)
          setMessage('Password must be at least 6 characters')
          setLoading(false)
          return
        }

        const { error } = await supabase.auth.signUp({
          email,
          password,
        })

        if (error) {
          setIsError(true)
          setMessage(error.message)
        } else {
          setMessage('Registration successful! Please check your email to confirm.')
          setEmail('')
          setPassword('')
          setConfirmPassword('')
        }
      }
    } catch (err) {
      setIsError(true)
      setMessage('An unexpected error occurred')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-box">
        <div className="auth-tabs">
          <button
            className={`tab ${isLogin ? 'active' : ''}`}
            onClick={() => setIsLogin(true)}
            type="button"
            disabled={loading}
          >
            Login
          </button>

          <button
            className={`tab ${!isLogin ? 'active' : ''}`}
            onClick={() => setIsLogin(false)}
            type="button"
            disabled={loading}
          >
            Register
          </button>
        </div>

        {message && (
          <div className={`message ${isError ? 'error' : 'success'}`}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">EMAIL ADDRESS</label>
            <input
              type="email"
              id="email"
              placeholder="user@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">PASSWORD</label>
            <input
              type="password"
              id="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label htmlFor="confirmPassword">CONFIRM PASSWORD</label>
              <input
                type="password"
                id="confirmPassword"
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                disabled={loading}
              />
            </div>
          )}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Processing...' : isLogin ? 'Login' : 'Register'}
          </button>

          <div className="divider">
            <span>OR</span>
          </div>

          <button type="button" className="google-btn" disabled={loading}>
            Login with Google
          </button>
        </form>

        <div className="back-link">
          <a href="/">← Back to Home</a>
        </div>
      </div>
    </div>
  )
}

export default App