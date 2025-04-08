import { FirebaseAuthentication } from '@capacitor-firebase/authentication';
import type { AuthStateChange, SignInResult, User } from '@capacitor-firebase/authentication';
import { ref } from 'vue';

// 현재 사용자 상태를 관리하는 반응형 참조
export const currentUser = ref<User | null>(null);
export const authReady = ref(false);

/**
 * GitHub로 로그인
 * @returns GitHub 로그인 결과를 포함한 Promise
 */
export const signInWithGithub = async (): Promise<SignInResult> => {
  try {
    const result = await FirebaseAuthentication.signInWithGithub();
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
      authReady.value = true;
    }
  );
  
  // 초기 사용자 상태 가져오기
  try {
    const result = await FirebaseAuthentication.getCurrentUser();
    currentUser.value = result.user;
  } catch (error) {
    console.error('현재 사용자 가져오기 오류:', error);
  }
  
  return listener.remove;
};

/**
 * 현재 사용자의 ID 토큰 가져오기
 * @param forceRefresh 토큰을 강제로 새로고침할지 여부
 * @returns ID 토큰 정보
 */
export const getIdToken = async (forceRefresh = false): Promise<string | null> => {
  try {
    const result = await FirebaseAuthentication.getIdToken({ forceRefresh });
    return result.token;
  } catch (error) {
    console.error('ID 토큰 가져오기 오류:', error);
    return null;
  }
};

/**
 * 현재 인증된 사용자 가져오기
 * @returns 현재 인증된 사용자 정보
 */
export const getCurrentUser = async (): Promise<User | null> => {
  try {
    const result = await FirebaseAuthentication.getCurrentUser();
    return result.user;
  } catch (error) {
    console.error('현재 사용자 가져오기 오류:', error);
    return null;
  }
}; 