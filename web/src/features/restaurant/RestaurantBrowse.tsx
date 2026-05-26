import type { Restaurant, UserRole } from '../../shared/types'

interface Props {
  restaurants: Restaurant[]
  userRole: UserRole
  onRefresh: () => void
  setMessage: (msg: string) => void
  setIsError: (v: boolean) => void
}

function RestaurantBrowse({ restaurants, userRole, onRefresh, setMessage, setIsError }: Props) {
  const openDiningRequest = (restaurantId: number) => {
    if (userRole !== 'DINER') {
      setIsError(true)
      setMessage('Only diner accounts can submit dining requests.')
      return
    }

    window.location.href = `/dining-request?restaurantId=${restaurantId}`
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
          <button
            type="button"
            key={r.id}
            className="card restaurant-card"
            onClick={() => { openDiningRequest(r.id) }}
          >
            <h4>{r.name}</h4>
            <p>{r.location}</p>
            <p className="muted">{r.cuisine}</p>
          </button>
        ))}
      </div>
    </section>
  )
}

export default RestaurantBrowse
