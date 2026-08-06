import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/styles/main.css'

const app = createApp(App)

app.directive('reveal', {
    mounted(el, binding) {
        el.classList.add('reveal-init')
        const delay = binding.value?.delay
        if (delay) el.style.transitionDelay = `${delay}ms`
        const io = new IntersectionObserver((entries) => {
            const entry = entries[0]
            if (entry.isIntersecting) {
                el.classList.add('is-revealed')
                io.disconnect()
            }
        }, {threshold: 0.12, rootMargin: '0px 0px -8% 0px'})
        el._revealObserver = io
        io.observe(el)
    },
    unmounted(el) {
        el._revealObserver?.disconnect()
    }
})

app.use(createPinia())
app.use(router)
app.mount('#app')
