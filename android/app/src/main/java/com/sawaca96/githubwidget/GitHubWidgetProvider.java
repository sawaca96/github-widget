package com.sawaca96.githubwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.util.Log;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import com.sawaca96.githubwidget.GithubWidgetConstant;
import android.view.View;
import android.net.Uri;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.sawaca96.githubwidget.utils.UIUtils;

public class GitHubWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "GitHubWidgetProvider";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String TOKEN_KEY = GithubWidgetConstant.TOKEN_KEY;
    private static final String USERNAME_KEY = GithubWidgetConstant.USERNAME_KEY;

    /**
     * 위젯의 처음 인스턴스가 배치될 때 호출됩니다.
     *
     * @param context 위젯이 실행되는 컨텍스트
     */
    @Override
    public void onEnabled(Context context) {
        try {
            Intent serviceIntent = new Intent(context, NotificationService.class);
            context.startService(serviceIntent);
            // TODO: 토스트 꼭 필요한 곳에만 쓰기
            UIUtils.showShortToast(context, context.getString(R.string.github_widget_enabled));
        } catch (Exception e) {
            Log.e(TAG, "서비스 시작 오류: " + e.getMessage());
        }
    }

    /**
     * 위젯의 마지막 인스턴스가 제거될 때 호출됩니다.
     *
     * @param context 위젯이 실행되는 컨텍스트
     */
    @Override
    public void onDisabled(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        context.stopService(intent);
        UIUtils.showShortToast(context, context.getString(R.string.github_widget_disabled));
    }

    /**
     * 위젯이 업데이트될 때마다 호출됩니다.
     * 다음과 같은 경우에 시스템에 의해 호출됩니다:
     * 1. 위젯이 처음 배치될 때
     * 2. 위젯의 업데이트 주기에 따라 정기적으로
     * 3. 앱에서 명시적으로 updateAppWidget()을 호출할 때
     * 
     * 이 메소드에서 위젯의 UI를 업데이트하고 필요한 서비스를 시작합니다.
     *
     * @param context          위젯이 실행되는 컨텍스트
     * @param appWidgetManager 위젯 업데이트를 위한 매니저
     * @param appWidgetIds     업데이트할 모든 위젯 ID 배열
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.github_widget_layout);
            updateAppWidget(context, appWidgetManager, appWidgetId, views);
        }

        try {
            Intent serviceIntent = new Intent(context, NotificationService.class);
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "서비스 시작 오류: " + e.getMessage());
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
            RemoteViews views) {
        try {
            // 앱으로 이동하는 인텐트 설정
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, getAppLaunchPendingIntent(context, appWidgetId));

            // 새로고침 버튼 클릭 리스너 설정
            Intent refreshIntent = new Intent(context, NotificationService.class);
            refreshIntent.setAction(GithubWidgetConstant.ACTION_UPDATE_NOTIFICATIONS);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
            PendingIntent refreshPendingIntent = PendingIntent.getService(
                    context,
                    appWidgetId * 100 + 2,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);

            // 위젯이 시각적으로 업데이트됨
            // UI 요소가 새 RemoteViews 객체에 정의된 대로 변경됨
            // onUpdate를 호출시키는 updateAppWidget이 아님
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "위젯 업데이트 오류", e);
        }
    }

    public static PendingIntent getAppLaunchPendingIntent(Context context, int appWidgetId) {
        Intent appLaunchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (appLaunchIntent == null) {
            return null;
        }

        appLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, appWidgetId * 100 + 1, appLaunchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}