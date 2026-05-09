import { useState } from 'react'
import './Dashboard.css'

interface DashboardProps {
  user?: { email?: string } | null
  onLogout?: () => void
  onRestaurantClick?: (restaurant: Restaurant) => void
}

interface Restaurant {
  id: number
  name: string
  description: string
  color: string
}

const restaurants: Restaurant[] = [
  {
    id: 1,
    name: 'Bella Italia',
    description: 'Authentic Italian cuisine with a modern twist',
    color: '#2563eb'
  },
  {
    id: 2,
    name: 'Sakura Restaurant',
    description: 'Premium Japanese dining experience',
    color: '#9333ea'
  },
  {
    id: 3,
    name: 'Bistro Rouge',
    description: 'Classic French bistro in the heart of the city',
    color: '#dc2626'
  },
  {
    id: 4,
    name: 'El Mariachi',
    description: 'Vibrant Mexican flavors and atmosphere',
    color: '#ea580c'
  },
  {
    id: 5,
    name: 'Spice Garden',
    description: 'Aromatic Indian cuisine and spices',
    color: '#16a34a'
  },
  {
    id: 6,
    name: 'Ocean Blue',
    description: 'Fresh seafood with coastal charm',
    color: '#0891b2'
  }
]

function Dashboard({ user, onLogout, onRestaurantClick }: DashboardProps) {
  const [searchQuery, setSearchQuery] = useState('')
  const [heroSearch, setHeroSearch] = useState('')
  const [cuisineFilter, setCuisineFilter] = useState('All Cuisines')
  const [locationFilter, setLocationFilter] = useState('All Locations')

  const handleHeroSearch = () => {
    setSearchQuery(heroSearch)
  }

  const clearFilters = () => {
    setSearchQuery('')
    setHeroSearch('')
    setCuisineFilter('All Cuisines')
    setLocationFilter('All Locations')
  }

  const filteredRestaurants = restaurants.filter(r => 
    r.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    r.description.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="dashboard">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-left">
          <div className="logo">
            <div className="logo-icon">DB</div>
            <span className="logo-text">DineBook</span>
          </div>
          <nav className="header-nav">
            <a href="#" className="nav-link">Home</a>
            <a href="#" className="nav-link active">Restaurants</a>
          </nav>
        </div>
        <div className="header-right">
          <div className="header-search">
            <span className="search-icon">🔍</span>
            <input 
              type="text" 
              placeholder="Search..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div className="user-menu">
            <div className="user-avatar">{user?.email?.[0]?.toUpperCase() || 'U'}</div>
            {onLogout && (
              <button className="logout-btn" onClick={onLogout}>
                Logout
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">Find Your Perfect<br />Dining Experience</h1>
          <p className="hero-subtitle">Discover exceptional restaurants and make dining requests with ease</p>
          <div className="hero-search-box">
            <input 
              type="text" 
              placeholder="Search restaurants..."
              value={heroSearch}
              onChange={(e) => setHeroSearch(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleHeroSearch()}
            />
            <button className="search-btn" onClick={handleHeroSearch}>Search</button>
          </div>
        </div>
      </section>

      {/* Filters Section */}
      <section className="filters-section">
        <div className="filter-group">
          <label>Cuisine Type</label>
          <select 
            value={cuisineFilter}
            onChange={(e) => setCuisineFilter(e.target.value)}
          >
            <option>All Cuisines</option>
            <option>Italian</option>
            <option>Japanese</option>
            <option>French</option>
            <option>Mexican</option>
            <option>Indian</option>
            <option>Seafood</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Location</label>
          <select 
            value={locationFilter}
            onChange={(e) => setLocationFilter(e.target.value)}
          >
            <option>All Locations</option>
            <option>Downtown</option>
            <option>Uptown</option>
            <option>Midtown</option>
            <option>Westside</option>
          </select>
        </div>
        <button className="clear-filters-btn" onClick={clearFilters}>
          Clear Filters
        </button>
      </section>

      {/* Results Section */}
      <section className="results-section">
        <h2 className="results-count">{filteredRestaurants.length} Restaurants Found</h2>
        <div className="restaurants-grid">
          {filteredRestaurants.map((restaurant) => (
            <div 
              key={restaurant.id} 
              className="restaurant-card"
              onClick={() => onRestaurantClick?.(restaurant)}
            >
              <div 
                className="card-image" 
                style={{ backgroundColor: restaurant.color }}
              />
              <div className="card-content">
                <h3 className="restaurant-name">{restaurant.name}</h3>
                <p className="restaurant-description">{restaurant.description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default Dashboard
