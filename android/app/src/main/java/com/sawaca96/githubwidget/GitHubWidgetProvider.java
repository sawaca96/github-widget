package com.sawaca96.githubwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class GitHubWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "GitHubWidgetProvider";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String UNIQUE_WORK_NAME = GithubWidgetConstant.UNIQUE_WORK_NAME;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(GithubWidgetConstant.ACTION_REFRESH)) {
            WidgetUpdateScheduler.scheduleUpdate(context);
        }
        super.onReceive(context, intent);
    }

    /**
     * 위젯의 마지막 인스턴스가 제거될 때 호출됩니다.
     *
     * @param context 위젯이 실행되는 컨텍스트
     */
    @Override
    public void onDisabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(GithubWidgetConstant.NOTIFICATIONS_KEY).apply();
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
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
        WidgetUpdateScheduler.scheduleUpdate(context);
    }
}