import { useState } from 'react'
import './Booking.css'
import { supabase } from '../supabaseClient'

interface Restaurant {
  id: number
  name: string
  description: string
  color: string
  cuisine: string
  location: string
  rating: number
  reviews: number
}

interface BookingProps {
  restaurant: Restaurant
  onBack: () => void
  user?: any
}

function Booking({ restaurant, onBack, user }: BookingProps) {
  const [selectedDate, setSelectedDate] = useState<string>('Fri 17 Apr')
  const [selectedTime, setSelectedTime] = useState<string>('')
  const [guests, setGuests] = useState<number>(2)
  const [specialRequests, setSpecialRequests] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [bookingSuccess, setBookingSuccess] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Generate next 8 days starting from today
  const generateDates = () => {
    const dates = []
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    const today = new Date()
    
    for (let i = 0; i < 8; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() + i)
      const dayName = days[date.getDay()]
      const dayNum = date.getDate()
      const month = months[date.getMonth()]
      dates.push(`${dayName} ${dayNum} ${month}`)
    }
    return dates
  }

  const dates = generateDates()

  const timeSlots = [
    '11:00 AM', '12:00 PM', '12:30 PM',
    '1:00 PM', '2:00 PM', '2:30 PM',
    '5:00 PM', '6:00 PM', '7:00 PM',
    '7:30 PM', '8:00 PM'
  ]

  const handleGuestsChange = (delta: number) => {
    const newCount = guests + delta
    if (newCount >= 1 && newCount <= 12) {
      setGuests(newCount)
    }
  }

  const handleSubmit = async () => {
    if (!selectedTime) {
      setError('Please select a time')
      return
    }

    if (!user) {
      setError('You must be logged in to make a booking')
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      // Parse date string to create a proper date
      const dateParts = selectedDate.split(' ')
      const dayNum = parseInt(dateParts[1])
      const monthName = dateParts[2]
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      const monthIndex = months.indexOf(monthName)
      const year = new Date().getFullYear()
      
      const bookingDate = new Date(year, monthIndex, dayNum)

      const { error: insertError } = await supabase
        .from('bookings')
        .insert([
          {
            user_id: user.id,
            restaurant_id: restaurant.id,
            restaurant_name: restaurant.name,
            booking_date: bookingDate.toISOString().split('T')[0],
            booking_time: selectedTime,
            guests: guests,
            special_requests: specialRequests || null,
            status: 'pending'
          }
        ])
        .select()

      if (insertError) {
        throw insertError
      }

      setBookingSuccess(true)
    } catch (err: any) {
      console.error('Booking error:', err)
      setError(err.message || 'Failed to create booking. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (bookingSuccess) {
    return (
      <div className="booking-page">
        <div className="booking-success">
          <div className="success-icon">✓</div>
          <h2>Booking Request Submitted!</h2>
          <p>Your reservation at <strong>{restaurant.name}</strong> has been received.</p>
          <div className="booking-details">
            <p><strong>Date:</strong> {selectedDate}</p>
            <p><strong>Time:</strong> {selectedTime}</p>
            <p><strong>Guests:</strong> {guests}</p>
          </div>
          <p className="status-note">Status: Pending confirmation</p>
          <button className="back-btn large" onClick={onBack}>
            Back to Restaurants
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="booking-page">
      {/* Header */}
      <header className="booking-header">
        <button className="back-btn" onClick={onBack}>
          ← Back to Restaurants
        </button>
      </header>

      {/* Restaurant Hero */}
      <div className="restaurant-hero" style={{ backgroundColor: restaurant.color }}>
        <div className="restaurant-info">
          <span className="cuisine-tag">{restaurant.cuisine}</span>
          <h1 className="restaurant-name">{restaurant.name}</h1>
          <p className="restaurant-location">{restaurant.location}</p>
          <div className="restaurant-rating">
            <span className="stars">{'★'.repeat(Math.floor(restaurant.rating))}</span>
            <span className="rating-number">{restaurant.rating}</span>
            <span className="reviews">({restaurant.reviews} reviews)</span>
          </div>
        </div>
      </div>

      {/* Booking Form */}
      <div className="booking-form">
        {/* Date Selection */}
        <section className="form-section">
          <label className="section-label">SELECT A DATE</label>
          <div className="date-selector">
            {dates.map((date) => (
              <button
                key={date}
                className={`date-btn ${selectedDate === date ? 'selected' : ''}`}
                onClick={() => setSelectedDate(date)}
              >
                <span className="day">{date.split(' ')[0]}</span>
                <span className="date-num">{date.split(' ')[1]}</span>
                <span className="month">{date.split(' ')[2]}</span>
              </button>
            ))}
          </div>
        </section>

        {/* Time Selection */}
        <section className="form-section">
          <label className="section-label">SELECT A TIME</label>
          <div className="time-selector">
            {timeSlots.map((time) => (
              <button
                key={time}
                className={`time-btn ${selectedTime === time ? 'selected' : ''}`}
                onClick={() => setSelectedTime(time)}
              >
                {time}
              </button>
            ))}
          </div>
        </section>

        {/* Guests */}
        <section className="form-section">
          <label className="section-label">GUESTS</label>
          <div className="guests-section">
            <div className="guests-label">
              <span className="guests-title">Number of guests</span>
              <span className="guests-max">Max 12 per booking</span>
            </div>
            <div className="guests-control">
              <button 
                className="guest-btn" 
                onClick={() => handleGuestsChange(-1)}
                disabled={guests <= 1}
              >
                −
              </button>
              <span className="guest-count">{guests}</span>
              <button 
                className="guest-btn" 
                onClick={() => handleGuestsChange(1)}
                disabled={guests >= 12}
              >
                +
              </button>
            </div>
          </div>
        </section>

        {/* Special Requests */}
        <section className="form-section">
          <label className="section-label">SPECIAL REQUESTS (optional)</label>
          <textarea
            className="special-requests"
            placeholder="Allergies, celebrations, seating preferences..."
            value={specialRequests}
            onChange={(e) => setSpecialRequests(e.target.value)}
            rows={3}
          />
        </section>

        {/* Summary */}
        <div className="booking-summary">
          <div className="summary-row">
            <span>Restaurant</span>
            <span className="summary-value">{restaurant.name}</span>
          </div>
          <div className="summary-row">
            <span>Date</span>
            <span className="summary-value">{selectedDate || '—'}</span>
          </div>
          <div className="summary-row">
            <span>Time</span>
            <span className="summary-value">{selectedTime || '—'}</span>
          </div>
          <div className="summary-row">
            <span>Guests</span>
            <span className="summary-value">{guests} guests</span>
          </div>
          
          {error && <div className="error-message">{error}</div>}
          
          <button 
            className="confirm-btn" 
            onClick={handleSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <span className="spinner"></span>
            ) : (
              <span className="arrow-down">↓</span>
            )}
          </button>
        </div>
      </div>
    </div>
  )
}

export default Booking
