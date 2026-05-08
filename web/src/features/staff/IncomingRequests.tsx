import type { DiningRequest } from '../../shared/types'
import { callBackend } from '../../shared/api'

interface Props {
  requests: DiningRequest[]
  onRefresh: () => void
  onMyRequestsRefresh: () => void
  setMessage: (msg: string) => void
  setIsError: (v: boolean) => void
}

function IncomingRequests({ requests, onRefresh, onMyRequestsRefresh, setMessage, setIsError }: Props) {
  const updateStatus = async (id: number, status: 'APPROVED' | 'REJECTED') => {
    try {
      await callBackend(`/staff/requests/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      setIsError(false)
      setMessage(`Request ${status.toLowerCase()}.`)
      onRefresh()
      onMyRequestsRefresh()
    } catch (err) {
      console.error(err)
      setIsError(true)
      setMessage(`Failed to ${status.toLowerCase()} request`)
    }
  }

  return (
    <section>
      <h3>Incoming Requests (Staff)</h3>
      <button type="button" className="small-btn" onClick={() => { setMessage(''); setIsError(false); onRefresh() }}>
        Refresh
      </button>
      <div className="list">
        {requests.length === 0 && <p>No incoming requests.</p>}
        {requests.map((r) => (
          <div key={r.id} className="card">
            <h4>
              {r.restaurantName}{' '}
              <span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span>
            </h4>
            <p>
              {new Date(r.requestedDateTime).toLocaleString()} · {r.guests} guest{r.guests > 1 ? 's' : ''} · {r.dinerEmail}
            </p>
            <div className="actions-row">
              <button type="button" className="small-btn" disabled={r.status === 'APPROVED'}
                onClick={() => updateStatus(r.id, 'APPROVED')}>
                Approve
              </button>
              <button type="button" className="small-btn danger" disabled={r.status === 'REJECTED'}
                onClick={() => updateStatus(r.id, 'REJECTED')}>
                Reject
              </button>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

export default IncomingRequests
