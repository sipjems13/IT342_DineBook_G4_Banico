import { useState } from 'react'
import type { Restaurant } from '../../shared/types'
import { callBackend } from '../../shared/api'

interface Props {
  restaurants: Restaurant[]
  onRefresh: () => void
  onRequestCreated: () => void
  setMessage: (msg: string) => void
  setIsError: (v: boolean) => void
}

function RestaurantBrowse({ restaurants, onRefresh, onRequestCreated, setMessage, setIsError }: Props) {
  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number | null>(null)
  const [requestDate, setRequestDate] = useState('')
  const [requestTime, setRequestTime] = useState('')
  const [requestGuests, setRequestGuests] = useState(2)

  const handleSubmitRequest = async () => {
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
      onRequestCreated()
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to submit dining request')
    }
  }

  return (
    <section>
      <h3>Browse Restaurants</h3>
      <button type="button" className="small-btn" onClick={() => { setMessage(''); setIsError(false); onRefresh() }}>
        Refresh
      </button>

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
        <select value={selectedRestaurantId ?? ''} onChange={(e) => setSelectedRestaurantId(e.target.value ? Number(e.target.value) : null)}>
          <option value="">Select restaurant...</option>
          {restaurants.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
        <input type="date" value={requestDate} onChange={(e) => setRequestDate(e.target.value)} />
        <input type="time" value={requestTime} onChange={(e) => setRequestTime(e.target.value)} />
        <input type="number" min={1} value={requestGuests} onChange={(e) => setRequestGuests(Number(e.target.value))} />
        <button type="button" className="small-btn" onClick={handleSubmitRequest}>Request</button>
      </div>
    </section>
  )
}

export default RestaurantBrowse
