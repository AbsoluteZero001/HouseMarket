import {ref} from 'vue'

export function useAlert() {
    const alertMsg = ref('')
    const alertType = ref('success')
    let timer = null

    function showAlert(msg, type = 'success', duration = 3200) {
        clearTimeout(timer)
        alertMsg.value = msg
        alertType.value = type
        timer = setTimeout(() => {
            alertMsg.value = ''
        }, duration)
    }

    function closeAlert() {
        clearTimeout(timer)
        alertMsg.value = ''
    }

    return {alertMsg, alertType, showAlert, closeAlert}
}
