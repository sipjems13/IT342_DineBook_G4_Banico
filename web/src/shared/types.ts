export type UserRole = 'DINER' | 'STAFF' | 'ADMIN' | null

export type Restaurant = {
  id: number
  name: string
  location: string
  cuisine: string
  imageUrl?: string | null
  rating?: number
}

export type DiningRequest = {
  id: number
  restaurantId: number
  restaurantName: string
  dinerEmail: string
  requestedDateTime: string
  guests: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
}
