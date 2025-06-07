<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>GitHub Widget</ion-title>
        <ion-buttons slot="end">
          <ion-button v-if="currentUser" @click="handleLogout">
            <ion-icon :icon="logOutOutline" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <div v-if="currentUser" class="content-container">
        <ion-card-content class="profile-card-content">
          <ion-avatar>
            <img :src="currentUser.photoUrl || ''" alt="Profile image" />
          </ion-avatar>
          <div>
            <ion-card-title>{{ currentUser.displayName }}</ion-card-title>
            <ion-card-subtitle>{{ currentUser.email }}</ion-card-subtitle>
          </div>
        </ion-card-content>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { logOutOutline } from 'ionicons/icons';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonIcon,
  IonAvatar,
  toastController,
  IonCardContent,
  IonCardTitle,
  IonCardSubtitle,
} from '@ionic/vue';
import { currentUser, logOut } from '@/services/auth';
import { clearGitHubToken } from '@/services/github';
import { useRouter } from 'vue-router';


const router = useRouter();


const handleLogout = async () => {
  try {
    await logOut();
    await clearGitHubToken();

    const toast = await toastController.create({
      message: 'You have been logged out.',
      duration: 2000,
      position: 'bottom'
    });

    await toast.present();

    await router.push('/login');
  } catch (error) {
    console.error('Logout error:', error);
  }
};
</script>

<style scoped>
.content-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.profile-card-content {
  display: flex;
  align-items: center;
}

ion-avatar {
  width: 80px;
  height: 80px;
  margin-right: 16px;
}

.not-logged-in {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.token-info {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 8px;
  word-break: break-all;
}

.debug-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.debug-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 6px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
}
</style>
