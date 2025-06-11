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
                <p>Sign in with your GitHub account to get started</p>

                <ion-spinner v-if="loading" name="crescent" class="spinner"></ion-spinner>
                <ion-button v-else @click="handleGithubLogin" expand="block" color="dark" class="github-button"
                    :disabled="loading">
                    <ion-icon :icon="logoGithub" slot="start"></ion-icon>
                    Sign in with GitHub
                </ion-button>

                <ion-text color="danger" v-if="error" class="error-message">
                    {{ error }}
                </ion-text>
            </div>
        </ion-content>
    </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
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
import { signInWithGithub } from '../services/auth';
import { Capacitor } from '@capacitor/core';

const router = useRouter();

const loading = ref(false);
const error = ref('');

const handleGithubLogin = async () => {
    loading.value = true;
    error.value = '';

    try {
        const result = await signInWithGithub();
        if (Capacitor.isNativePlatform()) {
            await handleNativeLogin(result);
        } else {
            handleWebLogin();
        }
    } catch (err: any) {
        error.value = 'An error occurred while logging in. Please try again.';
    } finally {
        loading.value = false;
    }
};

const handleWebLogin = () => {
    router.replace('/home');
};

const handleNativeLogin = async (result: any) => {
    const displayName = result.user?.displayName || result.user?.email?.split('@')[0] || 'github-user';

    const toast = await toastController.create({
        message: `Hello, ${displayName}! You are logged in.`,
        duration: 3000,
        position: 'bottom',
        color: 'success'
    });
    await toast.present();

    router.replace('/home');
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
    height: 36px;
    margin-top: 20px;
    margin-bottom: 4px;
}

.error-message {
    margin-top: 20px;
}
</style>