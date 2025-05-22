package com.sawaca96.githubwidget;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sawaca96.githubwidget.model.Notification;
import com.sawaca96.githubwidget.utils.UIUtils;

import java.util.List;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String TOKEN_KEY = GithubWidgetConstant.TOKEN_KEY;
    private static final String USERNAME_KEY = GithubWidgetConstant.USERNAME_KEY;
    private static final long UPDATE_INTERVAL = 30 * 60 * 1000L;
    private static final String CHANNEL_ID = "github_widget_channel";
    private static final String CHANNEL_NAME = "GitHub 위젯 알림";
    private static final String CHANNEL_DESC = "GitHub 위젯의 백그라운드 서비스 알림입니다.";
    private static final int NOTIFICATION_ID = 1001;
    private static final String NOTIFICATIONS_KEY = GithubWidgetConstant.NOTIFICATIONS_KEY;

    private GithubClient githubClient;
    private Handler mainHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 서비스가 처음 생성될 때 한 번만 호출됩니다.
     * 
     * 이 메서드는 다음과 같은 경우에 시스템에 의해 호출됩니다:
     * - 서비스가 startForegroundService() 또는 startService()로 최초 시작될 때
     * - 서비스가 종료된 후 다시 시작될 때
     * 
     * 이미 실행 중인 서비스를 다시 시작해도 이 메서드는 다시 호출되지 않습니다.
     * 서비스 생명주기 동안 한 번만 실행되며, 초기화 작업에 적합합니다.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.mainHandler = new Handler(Looper.getMainLooper());

    }

    /**
     * 서비스가 시작될 때마다 호출됩니다.
     * 
     * 이 메서드는 다음과 같은 경우에 시스템에 의해 호출됩니다:
     * - 서비스가 startForegroundService() 또는 startService()로 시작될 때마다
     * - 서비스가 이미 실행 중인 상태에서 재시작 요청을 받을 때마다
     * 
     * onCreate()와 달리 startService()가 호출될 때마다 실행되므로,
     * 주기적인 작업이나 새로운 인텐트에 응답하는 작업에 적합합니다.
     * 
     * 반환 값(START_STICKY 등)은 시스템에 의해 서비스가 강제 종료된 후
     * 어떻게 다시 시작할지를 결정합니다.
     * 
     * @param intent  서비스를 시작한 인텐트
     * @param flags   시작 플래그 (0, START_FLAG_REDELIVERY, START_FLAG_RETRY)
     * @param startId 시작 요청의 고유 ID
     * @return 서비스의 재시작 정책 (START_STICKY, START_NOT_STICKY 등)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && GithubWidgetConstant.ACTION_UPDATE_NOTIFICATIONS.equals(intent.getAction())) {
            this.updateNotifications();
        } else {
            this.updateNotifications();
        }
        return START_STICKY;
    }

    private void updateNotifications() {
        mainHandler.post(() -> updateWidgetLoadingState(true));

        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final String token = prefs.getString(TOKEN_KEY, null);
        final String username = prefs.getString(USERNAME_KEY, null);

        if (token == null || username == null) {
            this.updateWidgetsWithMessage(this.getString(R.string.github_login_required));
            prefs.edit().remove(NOTIFICATIONS_KEY).apply();
            notifyWidgetDataChanged();
            mainHandler.post(() -> updateWidgetLoadingState(false));
            return;
        }

        this.githubClient = new GithubClient(token);
        this.updateWidgetsWithMessage(username + this.getString(R.string.github_notification_title) + "을 확인 중...");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<com.sawaca96.githubwidget.model.Notification> notifications = this.githubClient
                        .fetchNotifications();

                saveNotificationsToPrefs(notifications);

                this.mainHandler.post(() -> {
                    if (notifications.isEmpty()) {
                        this.updateWidgetsWithMessage(username + this.getString(R.string.github_no_notifications));
                    } else {
                        this.updateWidgetsWithNotifications();
                    }
                    notifyWidgetDataChanged();
                    updateWidgetLoadingState(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "알림 가져오기 오류: " + e.getMessage(), e);
                this.mainHandler.post(() -> {
                    this.updateWidgetsWithMessage(this.getString(R.string.error_prefix, e.getMessage()));
                    prefs.edit().remove(NOTIFICATIONS_KEY).apply();
                    notifyWidgetDataChanged();
                    updateWidgetLoadingState(false);
                });
            }
        });
    }

    private void saveNotificationsToPrefs(List<Notification> notifications) {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (notifications == null || notifications.isEmpty()) {
            editor.remove(NOTIFICATIONS_KEY);
        } else {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String jsonString = gson.toJson(notifications);

                editor.putString(NOTIFICATIONS_KEY, jsonString);
            } catch (Exception e) {
                editor.remove(NOTIFICATIONS_KEY);
            }
        }
        editor.apply();
    }

    private void updateWidgetLoadingState(boolean isLoading) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, GitHubWidgetProvider.class));
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.github_widget_layout);

        if (isLoading) {
            views.setViewVisibility(R.id.btnRefresh, View.GONE);
            views.setViewVisibility(R.id.pbRefresh, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.btnRefresh, View.VISIBLE);
            views.setViewVisibility(R.id.pbRefresh, View.GONE);
        }

        for (int appWidgetId : appWidgetIds) {
            try {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                Log.e(TAG, "위젯 로딩 상태 업데이트 오류: " + appWidgetId, e);
            }
        }
    }

    private void updateWidgetsWithNotifications() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, GitHubWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            try {
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.github_widget_layout);
                views.setViewVisibility(R.id.lvNotifications, View.VISIBLE);
                views.setViewVisibility(R.id.tvNoNotifications, View.GONE);
                views.setViewVisibility(R.id.btnRefresh, View.VISIBLE);
                views.setViewVisibility(R.id.pbRefresh, View.GONE);

                Intent serviceIntent = new Intent(this, NotificationWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
                views.setRemoteAdapter(R.id.lvNotifications, serviceIntent);

                GitHubWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, views);

            } catch (Exception e) {
                Log.e(TAG, "위젯 업데이트 중 오류 발생: " + e.getMessage(), e);
            }
        }
    }

    private void updateWidgetsWithMessage(String message) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, GitHubWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            try {
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.github_widget_layout);
                views.setTextViewText(R.id.tvNoNotifications, message);
                views.setViewVisibility(R.id.tvNoNotifications, View.VISIBLE);
                views.setViewVisibility(R.id.lvNotifications, View.GONE);
                views.setViewVisibility(R.id.btnRefresh, View.VISIBLE);
                views.setViewVisibility(R.id.pbRefresh, View.GONE);

                GitHubWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, views);

            } catch (Exception e) {
                Log.e(TAG, "메시지 업데이트 중 오류 발생", e);
            }
        }
    }

    private void notifyWidgetDataChanged() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, GitHubWidgetProvider.class));
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lvNotifications);
        }
    }
}