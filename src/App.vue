<template>
  <ion-app>
    <ion-router-outlet v-if="authReady" />
    <div v-else class="loading-container">
      <ion-spinner name="crescent" />
    </div>
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet, IonSpinner } from '@ionic/vue';
import { onMounted, onUnmounted } from 'vue';
import { setupAuthStateListener, authReady } from '@/services/auth';

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
