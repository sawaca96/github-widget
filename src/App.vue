<template>
  <ion-app>
    <ion-router-outlet />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet } from '@ionic/vue';
import { onMounted, onUnmounted } from 'vue';
import { setupAuthStateListener } from '@/services/auth';

let removeAuthListener: (() => Promise<void>) | null = null;

onMounted(async () => {
  removeAuthListener = await setupAuthStateListener();
});

onUnmounted(async () => {
  if (removeAuthListener) {
    await removeAuthListener();
  }
});
</script>

<style>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
}
</style>
