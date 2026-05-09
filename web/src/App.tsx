/**
 * App.tsx — Vertical Slice Architecture entry point.
 *
 * Routes:
 *   /           → AuthPage  (login / register)
 *   /dashboard  → DashboardPage (browse, my requests, staff tools)
 *
 * Each feature lives in src/features/<slice>/ and owns its own
 * components, styles, and API calls.
 */
import AuthPage from './features/auth/AuthPage'
import DashboardPage from './features/dashboard/DashboardPage'
import LandingPage from './features/landing/LandingPage'

function App() {
  const path = window.location.pathname

  if (path === '/dashboard') {
    return <DashboardPage />
  }

  if (path === '/auth') {
    return <AuthPage />
  }

  return <LandingPage />
}

export default App
