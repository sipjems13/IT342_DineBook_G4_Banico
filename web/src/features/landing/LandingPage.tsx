import './LandingPage.css'

function LandingPage() {
  return (
    <div className="landing">
      <header className="landing-topbar">
        <div className="brand">
          <div className="brand-mark">DB</div>
          <div className="brand-text">
            <div className="brand-name">DineBook</div>
            <div className="brand-tagline">Book your dining experience, effortlessly.</div>
          </div>
        </div>
        <div className="landing-actions">
          <a className="btn btn-ghost" href="/auth">Sign in</a>
          <a className="btn btn-primary" href="/auth">Get started</a>
        </div>
      </header>

      <main className="landing-main">
        <section className="hero">
          <div className="hero-copy">
            <div className="pill">New • Simple dining requests</div>
            <h1>Book your dining experience online.</h1>
            <p>
              Browse restaurants, submit dining requests, and track approvals in one place.
              Staff can manage restaurants and incoming requests.
            </p>
            <div className="hero-cta">
              <a className="btn btn-primary" href="/auth">Login / Register</a>
              <a className="btn btn-ghost" href="/dashboard">Go to dashboard</a>
            </div>
          </div>

          <div className="hero-card">
            <div className="hero-card-header">
              <div className="hero-card-title">Quick actions</div>
              <div className="hero-card-sub">Preview of the workflow</div>
            </div>
            <div className="hero-card-grid">
              <div className="feature-card">
                <div className="feature-icon icon-blue">🍽️</div>
                <div className="feature-title">Browse</div>
                <div className="feature-desc">Find restaurants by location and cuisine.</div>
              </div>
              <div className="feature-card">
                <div className="feature-icon icon-violet">🗓️</div>
                <div className="feature-title">Request</div>
                <div className="feature-desc">Pick date/time and guests in seconds.</div>
              </div>
              <div className="feature-card">
                <div className="feature-icon icon-emerald">✅</div>
                <div className="feature-title">Track</div>
                <div className="feature-desc">See pending/approved/rejected status.</div>
              </div>
            </div>
            <div className="hero-card-footer">
              <div className="mini-stat">
                <div className="mini-stat-value">8080</div>
                <div className="mini-stat-label">Backend</div>
              </div>
              <div className="mini-stat">
                <div className="mini-stat-value">5173</div>
                <div className="mini-stat-label">Frontend</div>
              </div>
              <div className="mini-stat">
                <div className="mini-stat-value">Supabase</div>
                <div className="mini-stat-label">Auth</div>
              </div>
            </div>
          </div>
        </section>

        <section className="showcase">
          <div className="showcase-card">
            <div className="showcase-left">
              <div className="showcase-kicker">Diner dashboard</div>
              <div className="showcase-title">Browse and request</div>
              <div className="showcase-text">
                Clean cards, clear filters, and a compact request form — designed for speed.
              </div>
            </div>
            <div className="showcase-right">
              <div className="skeleton">
                <div className="s-row">
                  <div className="s-chip" />
                  <div className="s-chip" />
                  <div className="s-chip" />
                </div>
                <div className="s-grid">
                  <div className="s-card" />
                  <div className="s-card" />
                  <div className="s-card" />
                  <div className="s-card" />
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="landing-footer">
        <span>© {new Date().getFullYear()} DineBook</span>
        <span className="dot">•</span>
        <span className="muted">Modern UI colorway applied</span>
      </footer>
    </div>
  )
}

export default LandingPage

