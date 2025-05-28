package com.sawaca96.githubwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class GitHubWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "GitHubWidgetProvider";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;

    /**
     * 위젯의 마지막 인스턴스가 제거될 때 호출됩니다.
     *
     * @param context 위젯이 실행되는 컨텍스트
     */
    @Override
    public void onDisabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(GithubWidgetConstant.NOTIFICATIONS_KEY).apply();
    }

    /**
     * 위젯이 업데이트될 때마다 호출됩니다.
     * 다음과 같은 경우에 시스템에 의해 호출됩니다:
     * 1. 위젯이 처음 배치될 때
     * 2. 위젯의 업데이트 주기에 따라 정기적으로
     * 3. 앱에서 명시적으로 updateAppWidget()을 호출할 때
     * 
     * 이 메소드에서는 NotificationService를 시작하여 위젯 업데이트를 처리합니다.
     *
     * @param context          위젯이 실행되는 컨텍스트
     * @param appWidgetManager 위젯 업데이트를 위한 매니저
     * @param appWidgetIds     업데이트할 모든 위젯 ID 배열
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            Intent serviceIntent = new Intent(context, NotificationService.class);
            /*
             * Android 8.0 (API 레벨 26)부터 백그라운드에서 서비스 실행에 제한이 생겼습니다.
             * 앱이 백그라운드에 있을 때 일반 startService()를 호출하면 IllegalStateException이 발생할 수 있습니다.
             * ContextCompat.startForegroundService()는 이러한 제한 하에서도 서비스를 시작할 수 있게 합니다.
             * 호출되면, 서비스는 5초 이내에 startForeground()를 호출하여 사용자에게 보이는 알림을 표시해야 합니다.
             * 이는 Android 8.0 이상에서 서비스가 예기치 않게 종료될 위험이 있음을 의미합니다.
             */
            ContextCompat.startForegroundService(context, serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "서비스 시작 오류: " + e.getMessage());
        }
    }
}