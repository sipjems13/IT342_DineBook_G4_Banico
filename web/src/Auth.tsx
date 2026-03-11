import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { supabase } from './supabaseClient'

function Auth() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const handleAuthCallback = async () => {
      try {
        // Wait for Supabase to process the callback from the URL
        await new Promise(resolve => setTimeout(resolve, 500))
        
        const { data: { session }, error: sessionError } = await supabase.auth.getSession()
        
        if (sessionError) {
          throw sessionError
        }
        
        if (session) {
          // Session is valid, redirect to dashboard
          navigate('/dashboard')
        } else {
          setError('Authentication failed. Redirecting...')
          setTimeout(() => navigate('/'), 2000)
        }
      } catch (err: any) {
        console.error('Auth error:', err)
        setError(err.message || 'Authentication error')
        setTimeout(() => navigate('/'), 2000)
      } finally {
        setLoading(false)
      }
    }

    handleAuthCallback()
  }, [navigate])

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh',
      flexDirection: 'column',
      gap: '20px'
    }}>
      {loading ? (
        <>
          <div style={{ fontSize: '18px' }}>Completing login...</div>
          <div style={{ fontSize: '14px', color: '#666' }}>Please wait...</div>
        </>
      ) : (
        <>
          <div style={{ fontSize: '18px', color: '#ff6b6b' }}>{error}</div>
          <div style={{ fontSize: '14px', color: '#666' }}>Redirecting to login page...</div>
        </>
      )}
    </div>
  )
}

export default Auth
