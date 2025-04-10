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
import { ref, onMounted } from 'vue';
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
import { saveGitHubToken, getGitHubToken } from '../services/github';

const router = useRouter();
const loading = ref(false);
const error = ref('');

onMounted(() => {
    if (currentUser.value) {
        // 이미 로그인되어 있는 경우 토큰 확인
        checkTokenAndRedirect();
    }
});

/**
 * 저장된 GitHub 토큰 확인 후 홈으로 리다이렉트
 */
const checkTokenAndRedirect = async () => {
    try {
        const tokenInfo = await getGitHubToken();
        if (tokenInfo.token) {
            router.replace('/home');
        }
    } catch (err) {
        console.error('토큰 확인 오류:', err);
    }
};

const handleGithubLogin = async () => {
    loading.value = true;
    error.value = '';

    try {
        const result = await signInWithGithub();
        let tokenSaved = false;

        try {
            if (result.credential?.accessToken) {
                await saveGitHubToken(
                    result.credential.accessToken,
                    result.user?.uid,
                    result.user?.displayName || result.user?.email?.split('@')[0] || 'github-user'
                );
                tokenSaved = true;
            } else {
                console.error('GitHub 액세스 토큰이 없습니다.');
                error.value = 'GitHub 액세스 토큰을 받지 못했습니다. 다시 로그인해주세요.';
            }
        } catch (tokenError) {
            console.error('GitHub 토큰 저장 오류:', tokenError);
            error.value = 'GitHub 토큰 저장 중 오류가 발생했습니다. 다시 시도해주세요.';
        }

        const toastMessage = tokenSaved
            ? `안녕하세요, ${result.user?.displayName || '사용자'}님! GitHub 토큰이 저장되었습니다.`
            : `안녕하세요, ${result.user?.displayName || '사용자'}님! 주의: GitHub 토큰 저장에 실패했습니다.`;

        const toast = await toastController.create({
            message: toastMessage,
            duration: 3000,
            position: 'bottom',
            color: tokenSaved ? 'success' : 'warning'
        });

        await toast.present();

        if (tokenSaved) {
            // 토큰이 성공적으로 저장된 경우에만 홈으로 이동
            router.replace('/home');
        }
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