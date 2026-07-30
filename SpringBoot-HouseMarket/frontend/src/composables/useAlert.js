import {ref} from 'vue'

export function useAlert() {
    const alertMsg = ref('')
    const alertType = ref('success')

    function showAlert(msg, type = 'success') {
        alertMsg.value = msg
        alertType.value = type
        setTimeout(() => alertMsg.value = '', 3000)
    }

    return {alertMsg, alertType, showAlert}
}
