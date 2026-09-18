import {defineStore} from 'pinia'
import {ref} from 'vue'
import {
    createHouse,
    deleteHouse,
    deleteHouseImage,
    getHouseById,
    getHouses,
    getHousesByLandlord,
    getMyHouses,
    reorderHouseImages,
    setCoverImage,
    updateHouse,
    uploadHouseImage as uploadHouseImageApi
} from '../api/houses'

export const useHouseStore = defineStore('houses', () => {
  const houses = ref([])
  const currentHouse = ref(null)
  const total = ref(0)
  const loading = ref(false)

  async function fetchHouses(params) {
    loading.value = true
    try {
      const res = await getHouses(params)
      if (res.data.success) {
        houses.value = res.data.data.houses || []
        total.value = res.data.data.total || 0
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchHouseById(id) {
    const res = await getHouseById(id)
    if (res.data.success) {
      currentHouse.value = res.data.data.house
    }
    return res.data
  }

  async function fetchLandlordHouses(landlordId) {
    const res = await getHousesByLandlord(landlordId)
    if (res.data.success) {
      houses.value = res.data.data.houses || []
    }
    return res.data
  }

  async function fetchMyHouses() {
    const res = await getMyHouses()
    if (res.data.success) {
      houses.value = res.data.data.houses || []
    }
    return res.data
  }

  async function addHouse(data) {
    const res = await createHouse(data)
    return res.data
  }

  async function editHouse(id, data) {
    const res = await updateHouse(id, data)
    return res.data
  }

  async function removeHouse(id) {
    const res = await deleteHouse(id)
    return res.data
  }

    async function addHouseImage(houseId, file, imageType = 'OTHER', sortOrder = 0, isCover = false) {
        const res = await uploadHouseImageApi(houseId, file, imageType, sortOrder, isCover)
        return res.data
    }

    async function removeHouseImage(houseId, imageId) {
        const res = await deleteHouseImage(houseId, imageId)
        return res.data
    }

    async function markCoverImage(houseId, imageId) {
        const res = await setCoverImage(houseId, imageId)
        return res.data
    }

    async function sortHouseImages(houseId, imageIds) {
        const res = await reorderHouseImages(houseId, imageIds)
        return res.data
    }

    return {
        houses,
        currentHouse,
        total,
        loading,
        fetchHouses,
        fetchHouseById,
        fetchLandlordHouses,
        fetchMyHouses,
        addHouse,
        editHouse,
        removeHouse,
        addHouseImage,
        removeHouseImage,
        markCoverImage,
        sortHouseImages
    }
})
