import { useState } from 'react'
import type { Restaurant } from '../../shared/types'
import { callBackend } from '../../shared/api'

interface Props {
  restaurants: Restaurant[]
  onRefresh: () => void
  setMessage: (msg: string) => void
  setIsError: (v: boolean) => void
}

function ManageRestaurants({ restaurants, onRefresh, setMessage, setIsError }: Props) {
  const [name, setName] = useState('')
  const [location, setLocation] = useState('')
  const [cuisine, setCuisine] = useState('')
  const [imageUrl, setImageUrl] = useState('')

  const handleAdd = async () => {
    try {
      if (!name || !location || !cuisine) {
        setIsError(true)
        setMessage('Fill in name, location and cuisine.')
        return
      }
      await callBackend('/staff/restaurants', {
        method: 'POST',
        body: JSON.stringify({ name, location, cuisine, imageUrl: imageUrl || null }),
      })
      setIsError(false)
      setMessage('Restaurant created.')
      setName(''); setLocation(''); setCuisine(''); setImageUrl('')
      onRefresh()
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to create restaurant')
    }
  }

  const handleDelete = async (r: Restaurant) => {
    if (!window.confirm(`Delete ${r.name}?`)) return
    try {
      await callBackend(`/staff/restaurants/${r.id}`, { method: 'DELETE' })
      setIsError(false)
      setMessage('Restaurant deleted.')
      onRefresh()
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage('Failed to delete restaurant')
    }
  }

  return (
    <section>
      <h3>Manage Restaurants (Staff)</h3>
      <div className="form-vertical">
        <input placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} />
        <input placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} />
        <input placeholder="Cuisine" value={cuisine} onChange={(e) => setCuisine(e.target.value)} />
        <input placeholder="Image URL (optional)" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} />
        <button type="button" className="small-btn" onClick={handleAdd}>Add Restaurant</button>
      </div>

      <h4 style={{ marginTop: '1.5rem' }}>Existing Restaurants</h4>
      <button type="button" className="small-btn" onClick={() => { setMessage(''); setIsError(false); onRefresh() }}>
        Refresh
      </button>
      <div className="list">
        {restaurants.length === 0 && <p>No restaurants yet.</p>}
        {restaurants.map((r) => (
          <div key={r.id} className="card">
            <h4>{r.name}</h4>
            <p>{r.location}</p>
            <p className="muted">{r.cuisine}</p>
            <button type="button" className="small-btn danger" onClick={() => handleDelete(r)}>Delete</button>
          </div>
        ))}
      </div>
    </section>
  )
}

export default ManageRestaurants
