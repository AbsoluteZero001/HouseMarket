<template>
  <LoadingBar />
  <RouterView v-slot="{ Component }">
    <Transition name="page" mode="out-in">
      <component :is="Component"/>
    </Transition>
  </RouterView>
</template>

<script setup>
import LoadingBar from './components/LoadingBar.vue'
</script>
