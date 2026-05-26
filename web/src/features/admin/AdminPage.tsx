import { useEffect, useState, useCallback } from 'react'
import { supabase } from '../../shared/supabaseClient'
import { callBackend } from '../../shared/api'
import type { DiningRequest } from '../../shared/types'
import './AdminPage.css'

type AdminUser = { id: number; email: string; role: string }
type Tab = 'requests' | 'users'
type StatusFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'

function AdminPage() {
  const [userEmail, setUserEmail] = useState<string | null>(null)
  const [tab, setTab] = useState<Tab>('requests')
  const [requests, setRequests] = useState<DiningRequest[]>([])
  const [users, setUsers] = useState<AdminUser[]>([])
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState<{ msg: string; type: 'success' | 'error' } | null>(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newEmail, setNewEmail] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [newRole, setNewRole] = useState<'DINER' | 'STAFF' | 'ADMIN'>('DINER')
  const [submittingUser, setSubmittingUser] = useState(false)

  // ── auth guard ──────────────────────────────────────────────────────────
  useEffect(() => {
    supabase.auth.getUser().then(({ data }) => {
      if (!data.user) {
        window.location.href = '/auth'
        return
      }
      setUserEmail(data.user.email ?? null)
    })
  }, [])

  // ── toast helper ────────────────────────────────────────────────────────
  const showToast = (msg: string, type: 'success' | 'error') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  // ── data loaders ────────────────────────────────────────────────────────
  const loadRequests = useCallback(async () => {
    setLoading(true)
    try {
      const data = (await callBackend('/admin/requests')) as DiningRequest[]
      setRequests(data)
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Failed to load requests', 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  const loadUsers = useCallback(async () => {
    setLoading(true)
    try {
      const data = (await callBackend('/admin/users')) as AdminUser[]
      setUsers(data)
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Failed to load users', 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!userEmail) return
    void loadRequests()
    void loadUsers()
  }, [userEmail, loadRequests, loadUsers])

  // ── actions ─────────────────────────────────────────────────────────────
  const updateRequestStatus = async (id: number, status: 'APPROVED' | 'REJECTED') => {
    try {
      await callBackend(`/admin/requests/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      showToast(`Request #${id} ${status.toLowerCase()}.`, 'success')
      void loadRequests()
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Action failed', 'error')
    }
  }

  const updateUserRole = async (userId: number, role: string) => {
    try {
      await callBackend(`/admin/users/${userId}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role }),
      })
      showToast('User role updated.', 'success')
      void loadUsers()
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Action failed', 'error')
    }
  }

  const deleteRequest = async (id: number) => {
    if (!window.confirm(`Are you sure you want to delete request #${id}?`)) return
    try {
      await callBackend(`/admin/requests/${id}`, {
        method: 'DELETE',
      })
      showToast(`Request #${id} deleted.`, 'success')
      void loadRequests()
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Delete failed', 'error')
    }
  }

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newEmail || !newPassword) {
      showToast('Please fill in all fields.', 'error')
      return
    }
    if (newPassword.length < 6) {
      showToast('Password must be at least 6 characters.', 'error')
      return
    }
    setSubmittingUser(true)
    try {
      await callBackend('/admin/users', {
        method: 'POST',
        body: JSON.stringify({
          email: newEmail,
          password: newPassword,
          role: newRole,
        }),
      })
      showToast('Account created successfully.', 'success')
      setShowCreateModal(false)
      setNewEmail('')
      setNewPassword('')
      setNewRole('DINER')
      void loadUsers()
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Failed to create user', 'error')
    } finally {
      setSubmittingUser(false)
    }
  }

  // ── computed ────────────────────────────────────────────────────────────
  const filteredRequests = statusFilter === 'ALL'
    ? requests
    : requests.filter(r => r.status === statusFilter)

  const pendingCount = requests.filter(r => r.status === 'PENDING').length

  // ── render ──────────────────────────────────────────────────────────────
  return (
    <div className="admin-page">
      {/* ── Sidebar ── */}
      <aside className="admin-sidebar">
        <div className="admin-sidebar-header">
          <div className="admin-brand">
            <div className="admin-brand-mark">DB</div>
            <h2>DineBook</h2>
          </div>
          <span className="admin-brand-sub">Admin Panel</span>
          <p className="admin-user-info">{userEmail}</p>
        </div>

        <nav className="admin-nav">
          <div className="admin-nav-section">Management</div>
          <button
            type="button"
            className={`admin-nav-btn ${tab === 'requests' ? 'active' : ''}`}
            onClick={() => setTab('requests')}
          >
            <span className="nav-icon">📋</span>
            All Requests
            {pendingCount > 0 && <span className="admin-badge-count">{pendingCount}</span>}
          </button>
          <button
            type="button"
            className={`admin-nav-btn ${tab === 'users' ? 'active' : ''}`}
            onClick={() => setTab('users')}
          >
            <span className="nav-icon">👥</span>
            All Users
            <span className="admin-badge-count">{users.length}</span>
          </button>
        </nav>

        <div className="admin-sidebar-footer">
          <a className="admin-back-link" href="/dashboard">← Back to Dashboard</a>
          <button
            type="button"
            className="admin-logout-btn"
            onClick={async () => { await supabase.auth.signOut(); window.location.href = '/' }}
          >
            🚪 Sign out
          </button>
        </div>
      </aside>

      {/* ── Main ── */}
      <main className="admin-main">
        <div className="admin-topbar">
          <div className="admin-topbar-left">
            <h1>{tab === 'requests' ? 'All Dining Requests' : 'Registered Users'}</h1>
            <p>
              {tab === 'requests'
                ? `${requests.length} total · ${pendingCount} pending`
                : `${users.length} registered users`}
            </p>
          </div>
          <div style={{ display: 'flex', gap: '12px' }}>
            {tab === 'users' && (
              <button
                type="button"
                className="admin-create-btn"
                onClick={() => setShowCreateModal(true)}
              >
                ➕ Create Account
              </button>
            )}
            <button
              type="button"
              className="admin-refresh-btn"
              onClick={() => { tab === 'requests' ? void loadRequests() : void loadUsers() }}
            >
              ↺ Refresh
            </button>
          </div>
        </div>

        <div className="admin-body">
          {/* ── Requests Tab ── */}
          {tab === 'requests' && (
            <>
              <div className="admin-filters">
                {(['ALL', 'PENDING', 'APPROVED', 'REJECTED'] as StatusFilter[]).map(f => (
                  <button
                    key={f}
                    type="button"
                    className={`admin-filter-btn ${statusFilter === f ? 'active' : ''}`}
                    onClick={() => setStatusFilter(f)}
                  >
                    {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
                    {f === 'PENDING' && pendingCount > 0 && ` (${pendingCount})`}
                  </button>
                ))}
              </div>

              {loading && <p className="admin-empty">Loading…</p>}

              {!loading && filteredRequests.length === 0 && (
                <div className="admin-empty">No requests matching this filter.</div>
              )}

              {!loading && filteredRequests.length > 0 && (
                <div className="admin-table-wrapper">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Restaurant</th>
                        <th>Diner</th>
                        <th>Date &amp; Time</th>
                        <th>Guests</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredRequests.map(r => (
                        <tr key={r.id}>
                          <td style={{ color: 'var(--admin-muted)' }}>{r.id}</td>
                          <td style={{ fontWeight: 600 }}>{r.restaurantName}</td>
                          <td style={{ color: 'var(--admin-muted)', fontSize: 12 }}>{r.dinerEmail}</td>
                          <td style={{ fontSize: 12 }}>
                            {new Date(r.requestedDateTime).toLocaleString()}
                          </td>
                          <td>{r.guests}</td>
                          <td>
                            <span className={`status-badge ${r.status.toLowerCase()}`}>
                              {r.status === 'PENDING' ? '⏳' : r.status === 'APPROVED' ? '✅' : '❌'}
                              {' '}{r.status}
                            </span>
                          </td>
                          <td>
                            <button
                              type="button"
                              className="action-btn approve"
                              disabled={r.status === 'APPROVED'}
                              onClick={() => void updateRequestStatus(r.id, 'APPROVED')}
                            >
                              Approve
                            </button>
                            <button
                              type="button"
                              className="action-btn reject"
                              disabled={r.status === 'REJECTED'}
                              onClick={() => void updateRequestStatus(r.id, 'REJECTED')}
                            >
                              Decline
                            </button>
                            <button
                              type="button"
                              className="action-btn delete"
                              onClick={() => void deleteRequest(r.id)}
                            >
                              🗑️ Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}

          {/* ── Users Tab ── */}
          {tab === 'users' && (
            <>
              {loading && <p className="admin-empty">Loading…</p>}
              {!loading && users.length === 0 && (
                <div className="admin-empty">No registered users found.</div>
              )}
              {!loading && users.length > 0 && (
                <div className="admin-table-wrapper">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Change Role</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map(u => (
                        <tr key={u.id}>
                          <td style={{ color: 'var(--admin-muted)' }}>{u.id}</td>
                          <td style={{ fontWeight: 500 }}>{u.email}</td>
                          <td>
                            <span className={`status-badge ${u.role.toLowerCase()}`}>
                              {u.role}
                            </span>
                          </td>
                          <td>
                            <select
                              className="role-select"
                              value={u.role}
                              onChange={e => void updateUserRole(u.id, e.target.value)}
                            >
                              <option value="DINER">DINER</option>
                              <option value="STAFF">STAFF</option>
                              <option value="ADMIN">ADMIN</option>
                            </select>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {/* ── Create User Modal ── */}
      {showCreateModal && (
        <div className="admin-modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="admin-modal" onClick={e => e.stopPropagation()}>
            <div className="admin-modal-header">
              <h3>Create User Account</h3>
              <button
                type="button"
                className="admin-modal-close"
                onClick={() => setShowCreateModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleCreateUser} className="admin-modal-form">
              <div className="form-group">
                <label htmlFor="modal-email">Email Address</label>
                <input
                  id="modal-email"
                  type="email"
                  placeholder="name@example.com"
                  value={newEmail}
                  onChange={e => setNewEmail(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="modal-password">Password</label>
                <input
                  id="modal-password"
                  type="password"
                  placeholder="••••••••"
                  value={newPassword}
                  onChange={e => setNewPassword(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="modal-role">System Role</label>
                <select
                  id="modal-role"
                  value={newRole}
                  onChange={e => setNewRole(e.target.value as 'DINER' | 'STAFF' | 'ADMIN')}
                >
                  <option value="DINER">DINER</option>
                  <option value="STAFF">STAFF</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div className="admin-modal-actions">
                <button
                  type="button"
                  className="modal-btn cancel"
                  onClick={() => setShowCreateModal(false)}
                  disabled={submittingUser}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="modal-btn submit"
                  disabled={submittingUser}
                >
                  {submittingUser ? 'Creating…' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Toast ── */}
      {toast && (
        <div className={`admin-toast ${toast.type}`}>
          {toast.msg}
        </div>
      )}
    </div>
  )
}

export default AdminPage
