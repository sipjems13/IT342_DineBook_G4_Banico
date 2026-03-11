import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import './Dashboard.css'
import { supabase } from './supabaseClient'

function Dashboard() {
  const navigate = useNavigate()
  const [user, setUser] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Get current user on mount
  const getUser = async () => {
    try {
      const { data: { session }, error: sessionError } = await supabase.auth.getSession()
      
      if (sessionError || !session) {
        // No session, redirect to login
        navigate('/')
        return
      }

      setUser(session.user)
    } catch (err) {
      console.error('Error getting user:', err)
      navigate('/')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    getUser()

    // Listen for auth state changes
    const { data: { subscription } } = supabase.auth.onAuthStateChange((event, session) => {
      if (event === 'SIGNED_OUT' || !session) {
        navigate('/')
      } else {
        setUser(session.user)
      }
    })

    return () => {
      subscription?.unsubscribe()
    }
  }, [navigate])

  const handleLogout = async () => {
    await supabase.auth.signOut()
    navigate('/')
  }

  if (loading) {
    return <div className="dashboard-container"><div className="dashboard-popup">Loading...</div></div>
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-popup">
        <div className="popup-header">
          <h1>Welcome to DineBook</h1>
          <button className="close-btn" onClick={() => window.history.back()}>×</button>
        </div>

        <div className="popup-content">
          <div className="user-info">
            <div className="avatar">👤</div>
            <div>
              <p className="user-email">{user?.email || 'User'}</p>
              <p className="user-status">Logged in</p>
            </div>
          </div>

          <div className="dashboard-menu">
            <h2>Quick Links</h2>
            <ul>
              <li><a href="#restaurants">🍽️ Browse Restaurants</a></li>
              <li><a href="#reservations">📅 My Reservations</a></li>
              <li><a href="#favorites">❤️ Favorite Places</a></li>
              <li><a href="#profile">⚙️ Profile Settings</a></li>
            </ul>
          </div>

          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
