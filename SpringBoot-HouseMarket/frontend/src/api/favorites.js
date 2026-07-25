import http from './http'

export function getFavorites() {
  return http.get('/api/favorites')
}

export function addFavorite(data) {
  return http.post('/api/favorites', data)
}

export function removeFavorite(houseId) {
  return http.delete(`/api/favorites/${houseId}`)
}

export function checkFavorite(userId, houseId) {
  return http.get('/api/favorites/check', { params: { userId, houseId } })
}
