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
          <ion-card-content>
            <ion-button expand="block" @click="setupWidget" :disabled="widgetSetupLoading">
              <ion-icon :icon="appsOutline" slot="start"></ion-icon>
              GitHub 위젯 설정
            </ion-button>
            <ion-note v-if="widgetSetupSuccess" color="success">
              위젯 설정이 완료되었습니다. 홈 화면에 GitHub 위젯을 추가해보세요.
            </ion-note>

            <!-- 디버깅 정보 영역 추가 -->
            <div class="debug-section" v-if="debugVisible">
              <h4>디버깅 정보</h4>
              <ion-button expand="block" size="small" color="medium" @click="testConnection">
                플러그인 연결 테스트
              </ion-button>
              <p v-if="debugInfo" class="debug-info">{{ debugInfo }}</p>
            </div>

            <ion-button expand="block" fill="clear" size="small" @click="toggleDebug">
              {{ debugVisible ? '디버깅 정보 숨기기' : '디버깅 정보 표시' }}
            </ion-button>
          </ion-card-content>
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
import { logOutOutline, appsOutline } from 'ionicons/icons';
import { ref } from 'vue';
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
  IonCardContent,
  IonNote,
  toastController
} from '@ionic/vue';
import { currentUser, logOut } from '@/services/auth';
import { getGitHubToken } from '@/services/github';
import { Capacitor } from '@capacitor/core';
import { useRouter } from 'vue-router';

const widgetSetupLoading = ref(false);
const widgetSetupSuccess = ref(false);
const debugVisible = ref(false);
const debugInfo = ref('');

const platform = Capacitor.getPlatform();
debugInfo.value = `현재 플랫폼: ${platform}`;

const router = useRouter();


const toggleDebug = () => {
  debugVisible.value = !debugVisible.value;
};

const testConnection = async () => {
  try {
    debugInfo.value = '플러그인 연결 테스트 중...';

    const pluginList = Capacitor.isPluginAvailable('GitHubWidget')
      ? 'GitHubWidget 플러그인이 등록되어 있습니다.'
      : 'GitHubWidget 플러그인이 등록되어 있지 않습니다.';

    debugInfo.value = pluginList;

    try {
      const tokenInfo = await getGitHubToken();
      debugInfo.value += `\n토큰 상태: ${tokenInfo.token ? '있음' : '없음'}`;

      if (tokenInfo.username) {
        debugInfo.value += `\n사용자명: ${tokenInfo.username}`;
      }
    } catch (err: any) {
      debugInfo.value += `\n토큰 가져오기 시도 실패: ${err.message || JSON.stringify(err)}`;
    }
  } catch (error: any) {
    debugInfo.value = `연결 테스트 오류: ${error.message || JSON.stringify(error)}`;
  }
};


const setupWidget = async () => {
  widgetSetupLoading.value = true;
  widgetSetupSuccess.value = false;

  try {
    const tokenInfo = await getGitHubToken();

    if (!tokenInfo.token || !currentUser.value) {
      const toast = await toastController.create({
        message: 'GitHub 토큰이 없습니다. 다시 로그인해주세요.',
        duration: 3000,
        position: 'bottom',
        color: 'warning',
        buttons: [
          {
            text: '로그인',
            handler: () => {
              router.push('/login');
            }
          }
        ]
      });

      await toast.present();
      throw new Error('GitHub 토큰이 없습니다.');
    }

    // GitHub API 토큰 유효성 검증
    try {
      debugInfo.value = '토큰 유효성 검증 중...';
      const response = await fetch('https://api.github.com/notifications?per_page=1', {
        method: 'GET',
        headers: {
          'Authorization': `token ${tokenInfo.token}`,
          'Accept': 'application/vnd.github+json',
          'X-GitHub-Api-Version': '2022-11-28'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(`GitHub API 오류: ${response.status} - ${errorData.message || response.statusText}`);
      }

      // API 요청이 성공하면 토큰이 유효한 것
      debugInfo.value += '\n토큰 유효성 검증 성공';
    } catch (apiError: any) {
      console.error('GitHub API 검증 오류:', apiError);
      debugInfo.value += `\n토큰 유효성 검증 실패: ${apiError.message}`;

      const toast = await toastController.create({
        message: `GitHub 토큰이 유효하지 않습니다: ${apiError.message}`,
        duration: 3000,
        position: 'bottom',
        color: 'danger',
        buttons: [
          {
            text: '다시 로그인',
            handler: () => {
              router.push('/login');
            }
          }
        ]
      });

      await toast.present();
      throw new Error('GitHub 토큰이 유효하지 않습니다.');
    }

    widgetSetupSuccess.value = true;

    const toast = await toastController.create({
      message: 'GitHub 위젯 설정이 완료되었습니다.',
      duration: 2000,
      position: 'bottom'
    });

    await toast.present();
  } catch (error) {
    console.error('위젯 설정 오류:', error);

    if (String(error).includes('GitHub 토큰이 없습니다') ||
      String(error).includes('GitHub 토큰이 유효하지 않습니다')) {
      return;
    }

    const toast = await toastController.create({
      message: '위젯 설정 중 오류가 발생했습니다.',
      duration: 2000,
      position: 'bottom',
      color: 'danger'
    });

    await toast.present();
  } finally {
    widgetSetupLoading.value = false;
  }
};

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
