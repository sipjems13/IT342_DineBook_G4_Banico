import './LandingPage.css'

function LandingPage() {
  return (
    <div className="landing">
      <header className="landing-topbar">
        <div className="brand">
          <div className="brand-mark">DB</div>
          <div className="brand-text">
            <div className="brand-name">DineBook</div>
            <div className="brand-tagline">Dining requests, handled clearly.</div>
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
            <div className="pill">Simple dining requests</div>
            <h1>DineBook</h1>
            <p>
              Browse nearby restaurants, send a dining request, and follow every approval
              from one polished workspace.
            </p>
            <div className="hero-cta">
              <a className="btn btn-primary" href="/auth">Start booking</a>
              <a className="btn btn-ghost" href="/dashboard">View dashboard</a>
            </div>
          </div>

          <div className="hero-card">
            <div className="hero-card-header">
              <div>
                <div className="hero-card-title">Tonight's flow</div>
                <div className="hero-card-sub">From discovery to confirmation</div>
              </div>
              <span className="hero-status">Open</span>
            </div>
            <div className="hero-card-grid">
              <div className="feature-card">
                <div className="feature-icon icon-blue">01</div>
                <div>
                  <div className="feature-title">Browse</div>
                  <div className="feature-desc">Find restaurants by location and cuisine.</div>
                </div>
              </div>
              <div className="feature-card">
                <div className="feature-icon icon-violet">02</div>
                <div>
                  <div className="feature-title">Request</div>
                  <div className="feature-desc">Pick date, time, and guests in seconds.</div>
                </div>
              </div>
              <div className="feature-card">
                <div className="feature-icon icon-emerald">03</div>
                <div>
                  <div className="feature-title">Track</div>
                  <div className="feature-desc">See pending, approved, and rejected statuses.</div>
                </div>
              </div>
            </div>
            <div className="hero-card-footer">
              <div className="mini-stat">
                <div className="mini-stat-value">Fast</div>
                <div className="mini-stat-label">Requests</div>
              </div>
              <div className="mini-stat">
                <div className="mini-stat-value">Live</div>
                <div className="mini-stat-label">Statuses</div>
              </div>
              <div className="mini-stat">
                <div className="mini-stat-value">Staff</div>
                <div className="mini-stat-label">Tools</div>
              </div>
            </div>
          </div>
        </section>

        <section className="showcase">
          <div className="showcase-card">
            <div className="showcase-left">
              <div className="showcase-kicker">Diner dashboard</div>
              <div className="showcase-title">Browse and request without clutter</div>
              <div className="showcase-text">
                Clear filters, compact restaurant cards, and staff workflows that stay out of the way.
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
        <span>Copyright {new Date().getFullYear()} DineBook</span>
        <span className="dot">|</span>
        <span className="muted">Dining requests made easier</span>
      </footer>
    </div>
  )
}

export default LandingPage
