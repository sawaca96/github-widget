<template>
    <ion-page>
        <ion-header>
            <ion-toolbar>
                <ion-title>로그인</ion-title>
            </ion-toolbar>
        </ion-header>

        <ion-content :fullscreen="true" class="ion-padding">
            <div class="login-container">
                <h1>GitHub Widget</h1>
                <p>GitHub 계정으로 로그인하여 시작하세요</p>

                <ion-button @click="handleGithubLogin" expand="block" color="dark" class="github-button"
                    :disabled="loading">
                    <ion-icon :icon="logoGithub" slot="start"></ion-icon>
                    GitHub 계정으로 로그인
                </ion-button>

                <ion-spinner v-if="loading" name="crescent" class="spinner"></ion-spinner>

                <ion-text color="danger" v-if="error">
                    {{ error }}
                </ion-text>
            </div>
        </ion-content>
    </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { logoGithub } from 'ionicons/icons';
import {
    IonPage,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonIcon,
    IonSpinner,
    IonText,
    toastController
} from '@ionic/vue';
import { signInWithGithub, currentUser } from '../services/auth';

const router = useRouter();
const loading = ref(false);
const error = ref('');

watch(currentUser, (newUser) => {
    if (newUser) {
        router.replace('/home');
    }
});

onMounted(() => {
    if (currentUser.value) {
        router.replace('/home');
    }
});

const handleGithubLogin = async () => {
    loading.value = true;
    error.value = '';

    try {
        const result = await signInWithGithub();

        const toast = await toastController.create({
            message: `안녕하세요, ${result.user?.displayName || '사용자'}님!`,
            duration: 2000,
            position: 'bottom'
        });

        await toast.present();
    } catch (err: any) {
        error.value = '로그인 중 문제가 발생했습니다. 다시 시도해주세요.';
        console.error('로그인 오류:', err);
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    max-width: 400px;
    margin: 0 auto;
    text-align: center;
}

.github-button {
    margin-top: 20px;
    width: 100%;
}

.spinner {
    margin-top: 20px;
}
</style>