package com.sawaca96.githubwidget;

public class GithubWidgetConstant {
    public static final String PREFS_NAME = "com.sawaca96.githubwidget.GitHubWidgetProvider";
    public static final String TOKEN_KEY = "github_token";
    public static final String USERNAME_KEY = "github_username";
    public static final String USER_ID_KEY = "github_user_id";
    public static final String NOTIFICATIONS_KEY = "github_notifications";

    public static final String ACTION_REFRESH = "com.sawaca96.githubwidget.ACTION_REFRESH";
    public static final String UNIQUE_WORK_NAME = "GitHubWidgetUpdateWork";

    public static final int PENDING_INTENT_APP_LAUNCH = 1;
    public static final int PENDING_INTENT_REFRESH = 2;
    public static final int PENDING_INTENT_LOGIN = 3;
    public static final int PENDING_INTENT_ITEM_CLICK = 4;
}