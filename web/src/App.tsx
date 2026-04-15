import { useState, useEffect } from 'react'
import './App.css'
import { supabase, isSupabaseConfigured } from './supabaseClient'
import Dashboard from './components/Dashboard'
import Booking from './components/Booking'

function App() {
  const [isLogin, setIsLogin] = useState(true)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [isError, setIsError] = useState(false)
  const [user, setUser] = useState<any>(null)
  const [authChecked, setAuthChecked] = useState(false)
  const [selectedRestaurant, setSelectedRestaurant] = useState<any>(null)

  // Extended restaurant data with booking info
  const restaurantsData = [
    {
      id: 1,
      name: 'Bella Italia',
      description: 'Authentic Italian cuisine with a modern twist',
      color: '#2563eb',
      cuisine: 'Italian · Fine Dining',
      location: 'Poblacion, Makati · 0.5 km away',
      rating: 4.7,
      reviews: 328
    },
    {
      id: 2,
      name: 'Sakura Restaurant',
      description: 'Premium Japanese dining experience',
      color: '#9333ea',
      cuisine: 'Japanese · Sushi Bar',
      location: 'BGC, Taguig · 1.2 km away',
      rating: 4.8,
      reviews: 412
    },
    {
      id: 3,
      name: 'Bistro Rouge',
      description: 'Classic French bistro in the heart of the city',
      color: '#dc2626',
      cuisine: 'French · Bistro',
      location: 'Greenbelt, Makati · 0.8 km away',
      rating: 4.5,
      reviews: 256
    },
    {
      id: 4,
      name: 'El Mariachi',
      description: 'Vibrant Mexican flavors and atmosphere',
      color: '#ea580c',
      cuisine: 'Mexican · Casual',
      location: 'Malate, Manila · 2.1 km away',
      rating: 4.3,
      reviews: 189
    },
    {
      id: 5,
      name: 'Spice Garden',
      description: 'Aromatic Indian cuisine and spices',
      color: '#16a34a',
      cuisine: 'Indian · Vegetarian Friendly',
      location: 'Quezon City · 3.5 km away',
      rating: 4.6,
      reviews: 275
    },
    {
      id: 6,
      name: 'Ocean Blue',
      description: 'Fresh seafood with coastal charm',
      color: '#0891b2',
      cuisine: 'Seafood · Fine Dining',
      location: 'MOA Complex, Pasay · 4.2 km away',
      rating: 4.4,
      reviews: 198
    }
  ]

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
          setMessage('Registration successful! Redirecting...')
          setTimeout(() => {
            window.location.href = '/dashboard'
          }, 1500)
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
    setLoading(true)
    setMessage('')
    setIsError(false)

    try {
      const { error } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: {
          redirectTo: window.location.origin,
        },
      })

      if (error) {
        setIsError(true)
        setMessage(error.message)
      }
    } catch (err) {
      setIsError(true)
      setMessage('An unexpected error occurred during Google Login')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Check authentication status on mount
  useEffect(() => {
    const checkAuth = async () => {
      const { data: { session } } = await supabase.auth.getSession()
      setUser(session?.user ?? null)
      setAuthChecked(true)
    }
    checkAuth()

    // Listen for auth changes
    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null)
    })

    return () => subscription.unsubscribe()
  }, [])

  // Show loading while checking auth
  if (!authChecked) {
    return (
      <div className="auth-container">
        <div className="auth-box">
          <div className="loading-spinner">Loading...</div>
        </div>
      </div>
    )
  }

  // Show dashboard if authenticated
  if (user) {
    if (selectedRestaurant) {
      return (
        <Booking 
          restaurant={selectedRestaurant} 
          onBack={() => setSelectedRestaurant(null)}
          user={user}
        />
      )
    }
    return (
      <Dashboard 
        user={user} 
        onLogout={() => supabase.auth.signOut()}
        onRestaurantClick={(restaurant) => {
          const fullData = restaurantsData.find(r => r.id === restaurant.id)
          setSelectedRestaurant(fullData || restaurant)
        }}
      />
    )
  }

  // Show login form if not authenticated
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

          <button 
            type="button" 
            className="google-btn" 
            disabled={loading}
            onClick={handleGoogleLogin}
          >
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