import { useState } from 'react'
import { supabase, isSupabaseConfigured } from '../../shared/supabaseClient'
import './AuthPage.css'

function AuthPage() {
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
      setMessage('Supabase is not configured. Please add your credentials to .env.local')
      return
    }

    setLoading(true)
    setMessage('')
    setIsError(false)

    try {
      if (isLogin) {
        const { error } = await supabase.auth.signInWithPassword({ email, password })
        if (error) {
          setIsError(true)
          setMessage(error.message)
        } else {
          setMessage('Login successful. Redirecting...')
          setTimeout(() => { window.location.href = '/dashboard' }, 1000)
        }
      } else {
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
        const { error } = await supabase.auth.signUp({ email, password })
        if (error) {
          setIsError(true)
          setMessage(error.message)
        } else {
          setMessage('Registration successful. Redirecting...')
          setTimeout(() => { window.location.href = '/dashboard' }, 1000)
        }
      }
    } catch {
      setIsError(true)
      setMessage('An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleLogin = async () => {
    setLoading(true)
    try {
      const { error } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: { redirectTo: window.location.origin + '/dashboard' },
      })
      if (error) {
        setIsError(true)
        setMessage(error.message)
      }
    } catch {
      setIsError(true)
      setMessage('An unexpected error occurred during Google Login')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-box">
        <div className="auth-brand">
          <a className="auth-mark" href="/">DB</a>
          <div>
            <h1>{isLogin ? 'Welcome back' : 'Create your account'}</h1>
            <p>{isLogin ? 'Sign in to manage bookings and requests.' : 'Register to start sending dining requests.'}</p>
          </div>
        </div>

        <div className="auth-tabs">
          <button className={`tab ${isLogin ? 'active' : ''}`} onClick={() => setIsLogin(true)} type="button" disabled={loading}>
            Sign in
          </button>
          <button className={`tab ${!isLogin ? 'active' : ''}`} onClick={() => setIsLogin(false)} type="button" disabled={loading}>
            Register
          </button>
        </div>

        {message && <div className={`message ${isError ? 'error' : 'success'}`}>{message}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email address</label>
            <input type="email" id="email" placeholder="user@example.com" value={email}
              onChange={(e) => setEmail(e.target.value)} required disabled={loading} />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input type="password" id="password" placeholder="Enter your password" value={password}
              onChange={(e) => setPassword(e.target.value)} required disabled={loading} />
          </div>
          {!isLogin && (
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm password</label>
              <input type="password" id="confirmPassword" placeholder="Confirm your password" value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)} required disabled={loading} />
            </div>
          )}
          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Processing...' : isLogin ? 'Sign in' : 'Create account'}
          </button>
          <div className="divider"><span>or</span></div>
          <button type="button" className="google-btn" disabled={loading} onClick={handleGoogleLogin}>
            Continue with Google
          </button>
        </form>
        <div className="back-link"><a href="/">Back to home</a></div>
      </div>
    </div>
  )
}

export default AuthPage
