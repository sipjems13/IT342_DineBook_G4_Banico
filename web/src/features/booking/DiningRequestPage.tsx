import { useEffect, useMemo, useState } from 'react'
import { callBackend } from '../../shared/api'
import type { Restaurant, UserRole } from '../../shared/types'
import './DiningRequestPage.css'

function DiningRequestPage() {
  const restaurantId = useMemo(() => {
    const params = new URLSearchParams(window.location.search)
    const parsed = Number(params.get('restaurantId'))
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }, [])

  const [restaurant, setRestaurant] = useState<Restaurant | null>(null)
  const [requestDate, setRequestDate] = useState('')
  const [requestTime, setRequestTime] = useState('')
  const [requestGuests, setRequestGuests] = useState(2)
  const [message, setMessage] = useState('')
  const [isError, setIsError] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [userRole, setUserRole] = useState<UserRole>(null)

  useEffect(() => {
    const loadRestaurant = async () => {
      if (!restaurantId) {
        setIsError(true)
        setMessage('Choose a restaurant before submitting a dining request.')
        setIsLoading(false)
        return
      }

      try {
        const me = (await callBackend('/users/me')) as { email: string; role: Exclude<UserRole, null> }
        setUserRole(me.role)
        if (me.role !== 'DINER') {
          setIsError(true)
          setMessage('Only diner accounts can submit dining requests.')
          return
        }

        const data = (await callBackend(`/restaurants/${restaurantId}`)) as Restaurant
        setRestaurant(data)
      } catch (err) {
        console.error(err)
        setIsError(true)
        setMessage('Failed to load restaurant details.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadRestaurant()
  }, [restaurantId])

  const handleSubmitRequest = async () => {
    try {
      if (userRole !== 'DINER') {
        setIsError(true)
        setMessage('Only diner accounts can submit dining requests.')
        return
      }

      if (!restaurantId || !requestDate || !requestTime || requestGuests < 1) {
        setIsError(true)
        setMessage('Select date, time and guests.')
        return
      }

      setIsSubmitting(true)
      const requestedDateTime = new Date(`${requestDate}T${requestTime}`).toISOString()
      await callBackend('/dining-requests', {
        method: 'POST',
        body: JSON.stringify({
          restaurantId,
          requestedDateTime,
          guests: requestGuests,
        }),
      })

      setIsError(false)
      setMessage('Dining request submitted. You can now check it in My Requests.')
      setRequestDate('')
      setRequestTime('')
      setRequestGuests(2)
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to submit dining request.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="request-page">
      <div className="request-shell">
        <div className="request-top">
          <div>
            <p className="dashboard-kicker">Submit Dining Request</p>
            <h1>Confirm your visit</h1>
          </div>
          <a className="home-link" href="/dashboard">Back to restaurants</a>
        </div>

        {message && <div className={`message top ${isError ? 'error' : 'success'}`}>{message}</div>}

        <section className="request-panel">
          {isLoading && <p>Loading restaurant...</p>}

          {!isLoading && restaurant && (
            <>
              <div className="selected-restaurant">
                <h2>{restaurant.name}</h2>
                <p>{restaurant.location}</p>
                <p className="muted">{restaurant.cuisine}</p>
              </div>

              <div className="request-form">
                <label>
                  Date
                  <input type="date" value={requestDate} onChange={(e) => setRequestDate(e.target.value)} />
                </label>
                <label>
                  Time
                  <input type="time" value={requestTime} onChange={(e) => setRequestTime(e.target.value)} />
                </label>
                <label>
                  Guests
                  <input
                    type="number"
                    min={1}
                    value={requestGuests}
                    onChange={(e) => setRequestGuests(Number(e.target.value))}
                  />
                </label>
                <button type="button" className="small-btn" disabled={isSubmitting} onClick={handleSubmitRequest}>
                  {isSubmitting ? 'Submitting...' : 'Confirm Dining Request'}
                </button>
              </div>
            </>
          )}
        </section>
      </div>
    </main>
  )
}

export default DiningRequestPage
