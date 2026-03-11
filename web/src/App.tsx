import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import './App.css'
import { supabase, isSupabaseConfigured } from './supabaseClient'

function App() {
  const navigate = useNavigate()
  const [isLogin, setIsLogin] = useState(true)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [isError, setIsError] = useState(false)
  const [checkingAuth, setCheckingAuth] = useState(true)

  // Check if user is already logged in
  useEffect(() => {
    const checkSession = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        if (session) {
          navigate('/dashboard')
        }
      } catch (err) {
        console.error('Error checking session:', err)
      } finally {
        setCheckingAuth(false)
      }
    }

    checkSession()
  }, [navigate])

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
            navigate('/dashboard')
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

  const handleGoogleLogin = async () => {
    const { error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: `${window.location.origin}/`,
      },
    })

    if (error) {
      setIsError(true)
      setMessage(error.message)
    }
  }

  if (checkingAuth) {
    return (
      <div className="auth-container">
        <div className="auth-box">
          <div style={{ textAlign: 'center', padding: '40px 20px' }}>
            <p>Checking authentication...</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-container">
      <div className="auth-box">
        <style>{`
          .auth-header {
            text-align: center;
            color: white;
            position: relative;
            z-index: 1;
            margin-top: -100px;
            padding-top: 40px;
            padding-bottom: 20px;
          }
          .auth-header h1 {
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 8px;
          }
          .auth-header p {
            font-size: 14px;
            opacity: 0.95;
            margin: 0;
          }
        `}</style>
        <div className="auth-header">
          <h1>🍽️ DineBook</h1>
          <p>Reserve your perfect dining experience</p>
        </div>
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
            <label htmlFor="email">📧 Email Address</label>
            <input
              type="email"
              id="email"
              placeholder="your.email@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">🔐 Password</label>
            <input
              type="password"
              id="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
              autoComplete={isLogin ? 'current-password' : 'new-password'}
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label htmlFor="confirmPassword"> Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                placeholder="Re-enter your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                disabled={loading}
                autoComplete="new-password"
              />
            </div>
          )}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Processing...' : isLogin ? 'Login' : 'Register'}
          </button>

          <div className="divider">
            <span>OR</span>
          </div>

          <button 
            type="button" 
            className="google-btn" 
            disabled={loading}
            onClick={handleGoogleLogin}
          >
          Sign in with Google
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