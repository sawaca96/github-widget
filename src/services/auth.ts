import { FirebaseAuthentication } from '@capacitor-firebase/authentication';
import type { AuthStateChange, SignInResult, User } from '@capacitor-firebase/authentication';
import { ref } from 'vue';

export const currentUser = ref<User | null>(null);

/**
 * GitHub로 로그인
 * @returns GitHub 로그인 결과를 포함한 Promise
 */
export const signInWithGithub = async (): Promise<SignInResult> => {
  try {
    const result = await FirebaseAuthentication.signInWithGithub({
      scopes: ['notifications'],
    });
    currentUser.value = result.user;
    return result;
  } catch (error: any) {
    console.error('GitHub 로그인 오류:', error);
    throw error;
  }
};

/**
 * 로그아웃
 */
export const logOut = async (): Promise<void> => {
  try {
    await FirebaseAuthentication.signOut();
    currentUser.value = null;
  } catch (error) {
    console.error('로그아웃 오류:', error);
    throw error;
  }
};

/**
 * 인증 상태 변경 리스너 설정
 * @returns 리스너 구독 취소 함수
 */
export const setupAuthStateListener = async (): Promise<() => Promise<void>> => {
  const listener = await FirebaseAuthentication.addListener('authStateChange', 
    (change: AuthStateChange) => {
      currentUser.value = change.user;
    }
  );
  
  try {
    const result = await FirebaseAuthentication.getCurrentUser();
    currentUser.value = result.user;
  } catch (error) {
    console.error('현재 사용자 가져오기 오류:', error);
  }
  
  return listener.remove;
};

