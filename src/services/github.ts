import { registerPlugin } from '@capacitor/core';

interface GitHubWidgetPlugin {
  saveGitHubToken(options: { token: string; userId?: string; username?: string }): Promise<{ success: boolean }>;
  getGitHubToken(): Promise<{ token: string | null; userId: string | null; username: string | null }>;
  clearGitHubToken(): Promise<{ success: boolean }>;
}

const GitHubWidget = registerPlugin<GitHubWidgetPlugin>('GitHubWidget');

/**
 * GitHub 토큰을 Android 네이티브에 저장
 * @param token GitHub 액세스 토큰
 * @param userId GitHub 사용자 ID (선택사항)
 * @param username GitHub 사용자명 (선택사항)
 * @returns 저장 성공 여부를 포함한 Promise
 */
export const saveGitHubToken = async (
  token: string, 
  userId?: string, 
  username?: string
): Promise<{ success: boolean }> => {
    if (!GitHubWidget) {
      throw new Error('GitHubWidget 플러그인이 등록되지 않았습니다.');
    }
    
    const result = await GitHubWidget.saveGitHubToken({ token, userId, username });
    return result;
};

/**
 * Android 네이티브에서 GitHub 토큰 가져오기
 * @returns 저장된 GitHub 토큰 정보를 포함한 Promise
 */
export const getGitHubToken = async (): Promise<{ 
  token: string | null; 
  userId: string | null; 
  username: string | null 
}> => {
    if (!GitHubWidget) {
      throw new Error('GitHubWidget 플러그인이 등록되지 않았습니다.');
    }
    
    const result = await GitHubWidget.getGitHubToken();
    return result;
};

/**
 * Android 네이티브에서 GitHub 토큰 삭제
 * @returns 삭제 성공 여부를 포함한 Promise
 */
export const clearGitHubToken = async (): Promise<{ success: boolean }> => {
    if (!GitHubWidget) {
      throw new Error('GitHubWidget 플러그인이 등록되지 않았습니다.');
    }
    
    const result = await GitHubWidget.clearGitHubToken();
    return result;
}; 