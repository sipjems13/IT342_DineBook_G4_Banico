import { useEffect, useState } from 'react'
import { supabase } from '../../shared/supabaseClient'
import { callBackend } from '../../shared/api'
import type { UserRole, Restaurant, DiningRequest } from '../../shared/types'
import RestaurantBrowse from '../restaurant/RestaurantBrowse'
import MyRequests from '../booking/MyRequests'
import ManageRestaurants from '../staff/ManageRestaurants'
import IncomingRequests from '../staff/IncomingRequests'
import './DashboardPage.css'

type Tab = 'browse' | 'myRequests' | 'manageRestaurants' | 'incomingRequests'

const tabTitles: Record<Tab, string> = {
  browse: 'Browse restaurants',
  myRequests: 'My requests',
  manageRestaurants: 'Manage restaurants',
  incomingRequests: 'Incoming requests',
}

function DashboardPage() {
  const [userEmail, setUserEmail] = useState<string | null>(null)
  const [userRole, setUserRole] = useState<UserRole>(null)
  const [activeTab, setActiveTab] = useState<Tab>('browse')
  const [message, setMessage] = useState('')
  const [isError, setIsError] = useState(false)

  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [browseLocation, setBrowseLocation] = useState('Cebu City')
  const [browseCuisine, setBrowseCuisine] = useState('')
  const [browseQuery, setBrowseQuery] = useState('')

  const [myRequests, setMyRequests] = useState<DiningRequest[]>([])
  const [allRequests, setAllRequests] = useState<DiningRequest[]>([])

  useEffect(() => {
    const checkUser = async () => {
      const { data } = await supabase.auth.getUser()
      const emailFromSession = data.user?.email ?? null
      setUserEmail(emailFromSession)

      if (emailFromSession) {
        try {
          const { data: sessionData } = await supabase.auth.getSession()
          const accessToken = sessionData.session?.access_token
          if (!accessToken) return
          const res = await fetch('http://localhost:8080/users/me', {
            headers: { Authorization: `Bearer ${accessToken}` },
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
    const { data: { subscription } } = supabase.auth.onAuthStateChange(() => { void checkUser() })
    return () => { subscription.unsubscribe() }
  }, [])

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
    void loadRestaurants()
    void loadMyRequests()
    if (userRole === 'STAFF') void loadAllRequests()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userEmail, userRole])

  return (
    <div className="dashboard-container">
      <aside className="dashboard-sidebar">
        <div className="dashboard-header">
          <div className="dashboard-brand">
            <span className="dashboard-brand-mark">DB</span>
            <h2>DineBook</h2>
          </div>
          <p className="dashboard-user">
            {userEmail || 'Guest session'} {userRole && <span className="badge">{userRole}</span>}
          </p>
        </div>
        <nav className="dashboard-nav">
          <button type="button" className={`nav-item ${activeTab === 'browse' ? 'active' : ''}`} onClick={() => setActiveTab('browse')}>
            Browse Restaurants
          </button>
          <button type="button" className={`nav-item ${activeTab === 'myRequests' ? 'active' : ''}`} onClick={() => setActiveTab('myRequests')}>
            My Requests
          </button>
          {userRole === 'STAFF' && (
            <>
              <button type="button" className={`nav-item ${activeTab === 'manageRestaurants' ? 'active' : ''}`} onClick={() => setActiveTab('manageRestaurants')}>
                Manage Restaurants
              </button>
              <button type="button" className={`nav-item ${activeTab === 'incomingRequests' ? 'active' : ''}`} onClick={() => setActiveTab('incomingRequests')}>
                Incoming Requests
              </button>
            </>
          )}
          {userRole === 'ADMIN' && (
            <a
              href="/admin"
              style={{
                display: 'block',
                padding: '10px 16px',
                borderRadius: '8px',
                background: 'rgba(124,106,247,0.15)',
                color: '#a78bfa',
                textDecoration: 'none',
                fontWeight: 600,
                fontSize: '14px',
                marginTop: '4px',
                border: '1px solid rgba(124,106,247,0.25)',
              }}
            >
              🛡 Admin Panel
            </a>
          )}
        </nav>
        <button className="logout-btn" type="button" onClick={async () => { await supabase.auth.signOut(); window.location.href = '/' }}>
          Logout
        </button>
      </aside>

      <main className="dashboard-main">
        <div className="dashboard-top">
          <div>
            <p className="dashboard-kicker">Workspace</p>
            <h1>{tabTitles[activeTab]}</h1>
          </div>
          <a className="home-link" href="/">Home</a>
        </div>

        {message && <div className={`message top ${isError ? 'error' : 'success'}`}>{message}</div>}

        {activeTab === 'browse' && (
          <div className="filters">
            <input placeholder="Search name or cuisine" value={browseQuery} onChange={(e) => setBrowseQuery(e.target.value)} />
            <input placeholder="Location" value={browseLocation} onChange={(e) => setBrowseLocation(e.target.value)} />
            <input placeholder="Cuisine" value={browseCuisine} onChange={(e) => setBrowseCuisine(e.target.value)} />
          </div>
        )}

        {activeTab === 'browse' && (
          <RestaurantBrowse
            restaurants={restaurants}
            userRole={userRole}
            onRefresh={loadRestaurants}
            setMessage={setMessage}
            setIsError={setIsError}
          />
        )}

        {activeTab === 'myRequests' && (
          <MyRequests
            requests={myRequests}
            onRefresh={loadMyRequests}
            setMessage={setMessage}
            setIsError={setIsError}
          />
        )}

        {userRole === 'STAFF' && activeTab === 'manageRestaurants' && (
          <ManageRestaurants
            restaurants={restaurants}
            onRefresh={loadRestaurants}
            setMessage={setMessage}
            setIsError={setIsError}
          />
        )}

        {userRole === 'STAFF' && activeTab === 'incomingRequests' && (
          <IncomingRequests
            requests={allRequests}
            onRefresh={loadAllRequests}
            onMyRequestsRefresh={loadMyRequests}
            setMessage={setMessage}
            setIsError={setIsError}
          />
        )}
      </main>
    </div>
  )
}

export default DashboardPage
