import type { DiningRequest } from '../../shared/types'

interface Props {
  requests: DiningRequest[]
  onRefresh: () => void
  setMessage: (msg: string) => void
  setIsError: (v: boolean) => void
}

function MyRequests({ requests, onRefresh, setMessage, setIsError }: Props) {
  return (
    <section>
      <h3>My Requests</h3>
      <button type="button" className="small-btn" onClick={() => { setMessage(''); setIsError(false); onRefresh() }}>
        Refresh
      </button>
      <div className="list">
        {requests.length === 0 && <p>You have no dining requests yet.</p>}
        {requests.map((r) => (
          <div key={r.id} className="card">
            <h4>
              {r.restaurantName}{' '}
              <span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span>
            </h4>
            <p>
              {new Date(r.requestedDateTime).toLocaleString()} · {r.guests} guest{r.guests > 1 ? 's' : ''}
            </p>
            <p className="muted">Requested at {new Date(r.createdAt).toLocaleString()}</p>
          </div>
        ))}
      </div>
    </section>
  )
}

export default MyRequests
