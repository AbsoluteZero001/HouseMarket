import http from './http'

export function getHouses(params) {
  return http.get('/api/houses', { params })
}

export function getHouseById(id) {
  return http.get(`/api/houses/${id}`)
}

export function getMyHouses() {
  return http.get('/api/houses/my')
}

export function getHousesByLandlord(landlordId) {
  return http.get(`/api/houses/landlord/${landlordId}`)
}

export function createHouse(data) {
  return http.post('/api/houses/add', data)
}

export function updateHouse(id, data) {
  return http.put(`/api/houses/${id}`, data)
}

export function deleteHouse(id) {
  return http.delete(`/api/houses/${id}`)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('image', file)
  return http.post('/api/houses/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadHouseImage(houseId, file, imageType = 'OTHER', sortOrder = 0, isCover = false) {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('imageType', imageType)
    formData.append('sortOrder', sortOrder)
    formData.append('isCover', isCover)
    return http.post(`/api/houses/${houseId}/images`, formData, {
        headers: {'Content-Type': 'multipart/form-data'}
    })
}

export function deleteHouseImage(houseId, imageId) {
    return http.delete(`/api/houses/${houseId}/images/${imageId}`)
}

export function setCoverImage(houseId, imageId) {
    return http.put(`/api/houses/${houseId}/images/${imageId}/cover`)
}

export function reorderHouseImages(houseId, imageIds) {
    return http.put(`/api/houses/${houseId}/images/reorder`, imageIds)
}

export function getPublicHouses(params = {}) {
    return http.get('/api/public/houses', {params})
}

export function getPublicStats() {
    return http.get('/api/public/stats')
}
