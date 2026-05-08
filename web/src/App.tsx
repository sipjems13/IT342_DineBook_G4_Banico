import { useEffect, useState } from 'react'
import './App.css'
import { supabase, isSupabaseConfigured } from './supabaseClient'
import Dashboard from './components/Dashboard'
import Booking from './components/Booking'

type UserRole = 'DINER' | 'STAFF' | null

type Restaurant = {
  id: number
  name: string
  location: string
  cuisine: string
  imageUrl?: string | null
}

type DiningRequest = {
  id: number
  restaurantId: number
  restaurantName: string
  dinerEmail: string
  requestedDateTime: string
  guests: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
}

function App() {
  const [isLogin, setIsLogin] = useState(true)
  const [userEmail, setUserEmail] = useState<string | null>(null)
  const [userRole, setUserRole] = useState<UserRole>(null)
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

  const [activeTab, setActiveTab] = useState<'browse' | 'myRequests' | 'manageRestaurants' | 'incomingRequests'>(
    'browse',
  )

  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [browseLocation, setBrowseLocation] = useState('Cebu City')
  const [browseCuisine, setBrowseCuisine] = useState('')
  const [browseQuery, setBrowseQuery] = useState('')

  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number | null>(null)
  const [requestDate, setRequestDate] = useState('')
  const [requestTime, setRequestTime] = useState('')
  const [requestGuests, setRequestGuests] = useState(2)

  const [myRequests, setMyRequests] = useState<DiningRequest[]>([])
  const [allRequests, setAllRequests] = useState<DiningRequest[]>([])

  const [newRestaurantName, setNewRestaurantName] = useState('')
  const [newRestaurantLocation, setNewRestaurantLocation] = useState('')
  const [newRestaurantCuisine, setNewRestaurantCuisine] = useState('')
  const [newRestaurantImageUrl, setNewRestaurantImageUrl] = useState('')

  // Detect Supabase session on mount and after OAuth redirect
  useEffect(() => {
    const checkUser = async () => {
      if (!isSupabaseConfigured) return
      const { data } = await supabase.auth.getUser()
      const emailFromSession = data.user?.email ?? null
      setUserEmail(emailFromSession)

      if (emailFromSession) {
        try {
          const { data: sessionData } = await supabase.auth.getSession()
          const accessToken = sessionData.session?.access_token
          if (!accessToken) return

          const res = await fetch('http://localhost:8080/users/me', {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
          })

          if (res.ok) {
            const body = (await res.json()) as { email: string; role: Exclude<UserRole, null> }
            setUserRole(body.role)
          }
        } catch (err) {
          console.error('Failed to load user role', err)
        }
      } else {
        setUserRole(null)
      }
    }

    void checkUser()

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(() => {
      void checkUser()
    })

    return () => {
      subscription.unsubscribe()
    }
  }, [])

  const callBackend = async (path: string, init?: RequestInit) => {
    const { data } = await supabase.auth.getSession()
    const token = data.session?.access_token
    if (!token) {
      throw new Error('Not authenticated')
    }

    const res = await fetch(`http://localhost:8080${path}`, {
      ...(init || {}),
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        ...(init?.headers || {}),
      },
    })

    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Request failed with status ${res.status}`)
    }

    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      return res.json()
    }
    return null
  }

  const loadRestaurants = async () => {
    try {
      const params = new URLSearchParams()
      if (browseLocation) params.append('location', browseLocation)
      if (browseCuisine) params.append('cuisine', browseCuisine)
      if (browseQuery) params.append('q', browseQuery)

      const data = (await callBackend(`/restaurants?${params.toString()}`)) as Restaurant[]
      setRestaurants(data)
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to load restaurants')
    }
  }

  const loadMyRequests = async () => {
    try {
      const data = (await callBackend('/dining-requests/my')) as DiningRequest[]
      setMyRequests(data)
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to load your requests')
    }
  }

  const loadAllRequests = async () => {
    try {
      const data = (await callBackend('/staff/requests')) as DiningRequest[]
      setAllRequests(data)
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to load incoming requests')
    }
  }

  useEffect(() => {
    if (!userEmail) return
    if (window.location.pathname === '/dashboard') {
      void loadRestaurants()
      void loadMyRequests()
      if (userRole === 'STAFF') {
        void loadAllRequests()
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userEmail, userRole])

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
          }, 1000)
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
          }, 1000)
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
      // Do not clear loading immediately; OAuth will navigate away
      setLoading(false)
    }
  }

  // Simple "dashboard" view once user is logged in
  if (window.location.pathname === '/dashboard' && userEmail) {
    return (
      <div className="dashboard-container">
        <aside className="dashboard-sidebar">
          <div className="dashboard-header">
            <h2>DineBook</h2>
            <p className="dashboard-user">
              {userEmail}{' '}
              {userRole && <span className="badge">{userRole}</span>}
            </p>
          </div>
          <nav className="dashboard-nav">
            <button
              type="button"
              className={`nav-item ${activeTab === 'browse' ? 'active' : ''}`}
              onClick={() => setActiveTab('browse')}
            >
              Browse Restaurants
            </button>
            <button
              type="button"
              className={`nav-item ${activeTab === 'myRequests' ? 'active' : ''}`}
              onClick={() => setActiveTab('myRequests')}
            >
              My Requests
            </button>
            {userRole === 'STAFF' && (
              <>
                <button
                  type="button"
                  className={`nav-item ${activeTab === 'manageRestaurants' ? 'active' : ''}`}
                  onClick={() => setActiveTab('manageRestaurants')}
                >
                  Manage Restaurants
                </button>
                <button
                  type="button"
                  className={`nav-item ${activeTab === 'incomingRequests' ? 'active' : ''}`}
                  onClick={() => setActiveTab('incomingRequests')}
                >
                  Incoming Requests
                </button>
              </>
            )}
          </nav>
          <button
            className="logout-btn"
            type="button"
            onClick={async () => {
              await supabase.auth.signOut()
              window.location.href = '/'
            }}
          >
            Logout
          </button>
        </aside>

        <main className="dashboard-main">
          {message && (
            <div className={`message top ${isError ? 'error' : 'success'}`}>
              {message}
            </div>
          )}

          {activeTab === 'browse' && (
            <section>
              <h3>Browse Restaurants</h3>
              <div className="filters">
                <input
                  placeholder="Search name, cuisine…"
                  value={browseQuery}
                  onChange={(e) => setBrowseQuery(e.target.value)}
                />
                <input
                  placeholder="Location"
                  value={browseLocation}
                  onChange={(e) => setBrowseLocation(e.target.value)}
                />
                <input
                  placeholder="Cuisine"
                  value={browseCuisine}
                  onChange={(e) => setBrowseCuisine(e.target.value)}
                />
                <button
                  type="button"
                  className="small-btn"
                  onClick={() => {
                    setMessage('')
                    setIsError(false)
                    void loadRestaurants()
                  }}
                >
                  Refresh
                </button>
              </div>

              <div className="list">
                {restaurants.length === 0 && <p>No restaurants yet. Staff can add some.</p>}
                {restaurants.map((r) => (
                  <div
                    key={r.id}
                    className={`card ${selectedRestaurantId === r.id ? 'selected' : ''}`}
                    onClick={() => setSelectedRestaurantId(r.id)}
                  >
                    <h4>{r.name}</h4>
                    <p>{r.location}</p>
                    <p className="muted">{r.cuisine}</p>
                  </div>
                ))}
              </div>

              <h4 style={{ marginTop: '1.5rem' }}>Submit Dining Request</h4>
              <div className="request-form-inline">
                <select
                  value={selectedRestaurantId ?? ''}
                  onChange={(e) => setSelectedRestaurantId(e.target.value ? Number(e.target.value) : null)}
                >
                  <option value="">Select restaurant…</option>
                  {restaurants.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.name}
                    </option>
                  ))}
                </select>
                <input
                  type="date"
                  value={requestDate}
                  onChange={(e) => setRequestDate(e.target.value)}
                />
                <input
                  type="time"
                  value={requestTime}
                  onChange={(e) => setRequestTime(e.target.value)}
                />
                <input
                  type="number"
                  min={1}
                  value={requestGuests}
                  onChange={(e) => setRequestGuests(Number(e.target.value))}
                />
                <button
                  type="button"
                  className="small-btn"
                  onClick={async () => {
                    try {
                      if (!selectedRestaurantId || !requestDate || !requestTime || requestGuests < 1) {
                        setIsError(true)
                        setMessage('Select restaurant, date, time and guests.')
                        return
                      }
                      const dateTimeIso = new Date(`${requestDate}T${requestTime}`).toISOString()
                      await callBackend('/dining-requests', {
                        method: 'POST',
                        body: JSON.stringify({
                          restaurantId: selectedRestaurantId,
                          requestedDateTime: dateTimeIso,
                          guests: requestGuests,
                        }),
                      })
                      setIsError(false)
                      setMessage('Dining request submitted.')
                      void loadMyRequests()
                    } catch (err) {
                      console.error(err)
                      setIsError(true)
                      setMessage('Failed to submit dining request')
                    }
                  }}
                >
                  Request
                </button>
              </div>
            </section>
          )}

          {activeTab === 'myRequests' && (
            <section>
              <h3>My Requests</h3>
              <button
                type="button"
                className="small-btn"
                onClick={() => {
                  setMessage('')
                  setIsError(false)
                  void loadMyRequests()
                }}
              >
                Refresh
              </button>
              <div className="list">
                {myRequests.length === 0 && <p>You have no dining requests yet.</p>}
                {myRequests.map((r) => (
                  <div key={r.id} className="card">
                    <h4>
                      {r.restaurantName}{' '}
                      <span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span>
                    </h4>
                    <p>
                      {new Date(r.requestedDateTime).toLocaleString()} · {r.guests} guest
                      {r.guests > 1 ? 's' : ''}
                    </p>
                    <p className="muted">Requested at {new Date(r.createdAt).toLocaleString()}</p>
                  </div>
                ))}
              </div>
            </section>
          )}

          {userRole === 'STAFF' && activeTab === 'manageRestaurants' && (
            <section>
              <h3>Manage Restaurants (Staff)</h3>
              <div className="form-vertical">
                <input
                  placeholder="Name"
                  value={newRestaurantName}
                  onChange={(e) => setNewRestaurantName(e.target.value)}
                />
                <input
                  placeholder="Location"
                  value={newRestaurantLocation}
                  onChange={(e) => setNewRestaurantLocation(e.target.value)}
                />
                <input
                  placeholder="Cuisine"
                  value={newRestaurantCuisine}
                  onChange={(e) => setNewRestaurantCuisine(e.target.value)}
                />
                <input
                  placeholder="Image URL (optional)"
                  value={newRestaurantImageUrl}
                  onChange={(e) => setNewRestaurantImageUrl(e.target.value)}
                />
                <button
                  type="button"
                  className="small-btn"
                  onClick={async () => {
                    try {
                      if (!newRestaurantName || !newRestaurantLocation || !newRestaurantCuisine) {
                        setIsError(true)
                        setMessage('Fill in name, location and cuisine.')
                        return
                      }
                      await callBackend('/staff/restaurants', {
                        method: 'POST',
                        body: JSON.stringify({
                          name: newRestaurantName,
                          location: newRestaurantLocation,
                          cuisine: newRestaurantCuisine,
                          imageUrl: newRestaurantImageUrl || null,
                        }),
                      })
                      setIsError(false)
                      setMessage('Restaurant created.')
                      setNewRestaurantName('')
                      setNewRestaurantLocation('')
                      setNewRestaurantCuisine('')
                      setNewRestaurantImageUrl('')
                      void loadRestaurants()
                    } catch (err) {
                      console.error(err)
                      setIsError(true)
                      setMessage('Failed to create restaurant')
                    }
                  }}
                >
                  Add Restaurant
                </button>
              </div>

              <h4 style={{ marginTop: '1.5rem' }}>Existing Restaurants</h4>
              <button
                type="button"
                className="small-btn"
                onClick={() => {
                  setMessage('')
                  setIsError(false)
                  void loadRestaurants()
                }}
              >
                Refresh
              </button>
              <div className="list">
                {restaurants.length === 0 && <p>No restaurants yet.</p>}
                {restaurants.map((r) => (
                  <div key={r.id} className="card">
                    <h4>{r.name}</h4>
                    <p>{r.location}</p>
                    <p className="muted">{r.cuisine}</p>
                    <button
                      type="button"
                      className="small-btn danger"
                      onClick={async () => {
                        if (!window.confirm(`Delete ${r.name}?`)) return
                        try {
                          await callBackend(`/staff/restaurants/${r.id}`, {
                            method: 'DELETE',
                          })
                          setIsError(false)
                          setMessage('Restaurant deleted.')
                          void loadRestaurants()
                        } catch (err) {
                          console.error(err)
                          setIsError(true)
                          setMessage('Failed to delete restaurant')
                        }
                      }}
                    >
                      Delete
                    </button>
                  </div>
                ))}
              </div>
            </section>
          )}

          {userRole === 'STAFF' && activeTab === 'incomingRequests' && (
            <section>
              <h3>Incoming Requests (Staff)</h3>
              <button
                type="button"
                className="small-btn"
                onClick={() => {
                  setMessage('')
                  setIsError(false)
                  void loadAllRequests()
                }}
              >
                Refresh
              </button>
              <div className="list">
                {allRequests.length === 0 && <p>No incoming requests.</p>}
                {allRequests.map((r) => (
                  <div key={r.id} className="card">
                    <h4>
                      {r.restaurantName}{' '}
                      <span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span>
                    </h4>
                    <p>
                      {new Date(r.requestedDateTime).toLocaleString()} · {r.guests} guest
                      {r.guests > 1 ? 's' : ''} · {r.dinerEmail}
                    </p>
                    <div className="actions-row">
                      <button
                        type="button"
                        className="small-btn"
                        disabled={r.status === 'APPROVED'}
                        onClick={async () => {
                          try {
                            await callBackend(`/staff/requests/${r.id}/status`, {
                              method: 'PATCH',
                              body: JSON.stringify({ status: 'APPROVED' }),
                            })
                            setIsError(false)
                            setMessage('Request approved.')
                            void loadAllRequests()
                            void loadMyRequests()
                          } catch (err) {
                            console.error(err)
                            setIsError(true)
                            setMessage('Failed to approve request')
                          }
                        }}
                      >
                        Approve
                      </button>
                      <button
                        type="button"
                        className="small-btn danger"
                        disabled={r.status === 'REJECTED'}
                        onClick={async () => {
                          try {
                            await callBackend(`/staff/requests/${r.id}/status`, {
                              method: 'PATCH',
                              body: JSON.stringify({ status: 'REJECTED' }),
                            })
                            setIsError(false)
                            setMessage('Request rejected.')
                            void loadAllRequests()
                            void loadMyRequests()
                          } catch (err) {
                            console.error(err)
                            setIsError(true)
                            setMessage('Failed to reject request')
                          }
                        }}
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}
        </main>
      </div>
    )
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