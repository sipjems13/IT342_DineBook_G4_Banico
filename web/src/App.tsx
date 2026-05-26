/**
 * App.tsx — Vertical Slice Architecture entry point.
 *
 * Routes:
 *   /           → LandingPage
 *   /auth       → AuthPage  (login / register)
 *   /dashboard  → DashboardPage (browse, my requests, staff tools)
 *   /admin      → AdminPage (admin-only panel)
 *
 * Each feature lives in src/features/<slice>/ and owns its own
 * components, styles, and API calls.
 */
import AuthPage from './features/auth/AuthPage'
import DiningRequestPage from './features/booking/DiningRequestPage'
import DashboardPage from './features/dashboard/DashboardPage'
import LandingPage from './features/landing/LandingPage'
import AdminPage from './features/admin/AdminPage'

function App() {
  const path = window.location.pathname

  if (path === '/admin') {
    return <AdminPage />
  }

  if (path === '/dashboard') {
    return <DashboardPage />
  }

  if (path === '/dining-request') {
    return <DiningRequestPage />
  }

  if (path === '/auth') {
    return <AuthPage />
  }

  return <LandingPage />
}

export default App
