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
      <div v-if="currentUser" class="profile-container">
        <ion-avatar>
          <img :src="currentUser.photoUrl || ''" alt="프로필 이미지" />
        </ion-avatar>
        <h2>{{ currentUser.displayName }}</h2>
        <p>{{ currentUser.email }}</p>

        <ion-card>
          <ion-card-header>
            <ion-card-title>GitHub 연결 성공</ion-card-title>
          </ion-card-header>
        </ion-card>
      </div>

      <div v-else class="not-logged-in">
        <p>로그인이 필요합니다.</p>
        <ion-button expand="block" router-link="/login">
          로그인 페이지로 이동
        </ion-button>
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
  IonCard,
  IonCardHeader,
  IonCardTitle,
  toastController
} from '@ionic/vue';
import { currentUser, logOut } from '@/services/auth';

const handleLogout = async () => {
  try {
    await logOut();

    const toast = await toastController.create({
      message: '로그아웃되었습니다.',
      duration: 2000,
      position: 'bottom'
    });

    await toast.present();
  } catch (error) {
    console.error('로그아웃 오류:', error);
  }
};
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

ion-avatar {
  width: 100px;
  height: 100px;
  margin-bottom: 16px;
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
</style>
