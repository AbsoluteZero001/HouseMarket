import {defineStore} from 'pinia'
import {ref} from 'vue'
import {
  approveAppointment,
  cancelAppointment,
  completeAppointment,
  createAppointment,
  deleteAppointment,
  getAppointments,
  rejectAppointment
} from '../api/appointments'

export const useAppointmentStore = defineStore('appointments', () => {
  const appointments = ref([])
  const loading = ref(false)

  async function fetchAppointments(status) {
    loading.value = true
    try {
      const res = await getAppointments(status)
      if (res.data.success) {
        appointments.value = res.data.data.appointments || []
      }
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function addAppointment(data) {
    const res = await createAppointment(data)
    return res.data
  }

  async function approve(id) {
    const res = await approveAppointment(id)
    return res.data
  }

  async function reject(id) {
    const res = await rejectAppointment(id)
    return res.data
  }

  async function cancel(id) {
    const res = await cancelAppointment(id)
    return res.data
  }

    async function complete(id) {
        const res = await completeAppointment(id)
        return res.data
    }

  async function remove(id) {
    const res = await deleteAppointment(id)
    return res.data
  }

    return {appointments, loading, fetchAppointments, addAppointment, approve, reject, cancel, complete, remove}
})
