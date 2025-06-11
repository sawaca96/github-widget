import { FirebaseAuthentication } from "@capacitor-firebase/authentication";
import type {
  AuthStateChange,
  SignInResult,
  User,
} from "@capacitor-firebase/authentication";
import { ref } from "vue";
import { saveGitHubToken, clearGitHubToken } from "./github";

export const currentUser = ref<User | null>(null);

/**
 * GitHub로 로그인
 * @returns GitHub 로그인 결과를 포함한 Promise
 */
export const signInWithGithub = async (): Promise<SignInResult> => {
  const result = await FirebaseAuthentication.signInWithGithub({
    scopes: ["notifications"],
  });
  const credential = result.credential;
  const user = result.user;

  if (user && credential?.accessToken) {
    await saveGitHubToken(
      credential.accessToken,
      user.uid,
      user.displayName || undefined
    );
  } else {
    await logOut();
    throw new Error("액세스 토큰을 가져올 수 없습니다.");
  }
  return result;
};

/**
 * 로그아웃
 */
export const logOut = async (): Promise<void> => {
  await FirebaseAuthentication.signOut();
  currentUser.value = null;
  await clearGitHubToken();
};

/**
 * 인증 상태 변경 리스너 설정
 * @returns 리스너 구독 취소 함수
 */
export const setupAuthStateListener = async (): Promise<
  () => Promise<void>
> => {
  const listener = await FirebaseAuthentication.addListener(
    "authStateChange",
    async (change: AuthStateChange) => {
      currentUser.value = change.user;
    }
  );
  return listener.remove;
};
