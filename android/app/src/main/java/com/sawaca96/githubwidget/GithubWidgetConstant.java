package com.sawaca96.githubwidget;

public class GithubWidgetConstant {
    public static final String PREFS_NAME = "GitHubWidgetPrefs";
    public static final String TOKEN_KEY = "github_token";
    public static final String USERNAME_KEY = "github_username";
    public static final String USER_ID_KEY = "github_user_id";
    public static final String NOTIFICATIONS_KEY = "widget_notifications";

    // PendingIntent 요청 코드
    public static final int PENDING_INTENT_APP_LAUNCH = 1;
    public static final int PENDING_INTENT_REFRESH = 2;

    // 에러 메시지
    public static final String ERROR_NETWORK = "네트워크 연결을 확인해주세요";
    public static final String ERROR_AUTH = "GitHub 인증이 만료되었습니다";
    public static final String ERROR_PARSING = "데이터 처리 중 오류가 발생했습니다";
}