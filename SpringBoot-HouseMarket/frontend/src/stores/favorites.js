import {defineStore} from 'pinia'
import {ref} from 'vue'
import {addFavorite, checkFavorite, getFavorites, removeFavorite} from '../api/favorites'

export const useFavoriteStore = defineStore('favorites', () => {
  const favorites = ref([])
  const loading = ref(false)

  async function fetchFavorites() {
    loading.value = true
    try {
      const res = await getFavorites()
      if (res.data.success) {
        favorites.value = res.data.data.favorites || []
      }
      return res.data
    } finally {
      loading.value = false
    }
  }

    async function add(houseId) {
        const res = await addFavorite({houseId})
    return res.data
  }

  async function remove(houseId) {
    const res = await removeFavorite(houseId)
    return res.data
  }

    async function check(houseId) {
        const res = await checkFavorite(houseId)
    return res.data
  }

  function isFavorited(houseId) {
    return favorites.value.some(f => f.houseId === houseId || (f.house && f.house.id === houseId))
  }

  return { favorites, loading, fetchFavorites, add, remove, check, isFavorited }
})
