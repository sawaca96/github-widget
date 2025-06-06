package com.sawaca96.githubwidget;

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
import androidx.core.app.NotificationCompat;

import com.sawaca96.githubwidget.model.Notification;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String TOKEN_KEY = GithubWidgetConstant.TOKEN_KEY;
    private static final String USERNAME_KEY = GithubWidgetConstant.USERNAME_KEY;
    private static final String NOTIFICATIONS_KEY = GithubWidgetConstant.NOTIFICATIONS_KEY;
    private static final String CHANNEL_ID = "GitHubWidgetChannel";
    private static final int NOTIFICATION_ID = 1;

    private Handler mainHandler;
    private ExecutorService executorService;

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
        this.executorService = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    /**
     * Android 8.0 (API 레벨 26) 이상에서 백그라운드 서비스 실행에 제약이 생기면서,
     * 백그라운드에서 시작된 서비스가 계속 실행되려면 `startForegroundService()`로 시작된 후
     * 5초 이내에 `startForeground()`를 호출하여 포그라운드 서비스로 전환해야 합니다.
     * `startForeground()`는 사용자에게 보이는 알림(Notification)을 필요로 하며,
     * Android 8.0부터 이러한 모든 알림은 알림 채널(Notification Channel)에 속해야 합니다.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
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
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title_syncing))
                .setContentText(getString(R.string.notification_message_syncing))
                .setSmallIcon(R.drawable.ic_github)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        this.updateNotifications();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.shutdown();
        }
    }

    private void updateNotifications() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, GitHubWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        if (appWidgetIds == null || appWidgetIds.length == 0) {
            Log.w(TAG, "업데이트할 위젯이 없음 - 서비스 종료");
            stopSelf();
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = createBaseRemoteViews(appWidgetId);
            updateWidgetViewVisibility(views, "loading");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final String token = prefs.getString(TOKEN_KEY, "");
        final String username = prefs.getString(USERNAME_KEY, "");

        if (token.isEmpty() || username.isEmpty()) {
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = createBaseRemoteViews(appWidgetId);
                updateWidgetViewVisibility(views, "login_required");

                Intent loginIntent = new Intent(this, MainActivity.class);
                loginIntent.putExtra("navigate_to_login", true);
                PendingIntent loginPendingIntent = PendingIntent.getActivity(
                        this,
                        appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_LOGIN,
                        loginIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.tvEmptyNotifications, loginPendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            return;
        }

        GithubClient githubClient = new GithubClient(token);

        this.executorService.execute(() -> {
            List<com.sawaca96.githubwidget.model.Notification> notifications = null;
            try {
                notifications = githubClient.fetchNotifications();
            } catch (Exception e) {
                Log.w(TAG, "알림 가져오기 실패: " + e.getMessage(), e);
            } finally {
                saveNotificationsToPrefs(notifications);
                List<Notification> finalNotifications = notifications;

                this.mainHandler.post(() -> {
                    for (int appWidgetId : appWidgetIds) {
                        RemoteViews views = createBaseRemoteViews(appWidgetId);
                        if (finalNotifications == null) {
                            updateWidgetViewVisibility(views, "error");
                        } else if (finalNotifications.isEmpty()) {
                            updateWidgetViewVisibility(views, "empty");
                        } else {
                            Intent remoteAdapterIntent = new Intent(this, NotificationRemoteViewsService.class);
                            views.setRemoteAdapter(R.id.lvNotifications, remoteAdapterIntent);

                            Intent clickIntentTemplate = new Intent(Intent.ACTION_VIEW);

                            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                flags |= PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;
                            }

                            PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(
                                    this,
                                    appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_ITEM_CLICK,
                                    clickIntentTemplate,
                                    flags);
                            views.setPendingIntentTemplate(R.id.lvNotifications, clickPendingIntentTemplate);

                            updateWidgetViewVisibility(views, "list");
                        }

                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                    if (finalNotifications != null && !finalNotifications.isEmpty()) {
                        notifyWidgetDataChanged();
                    }
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
                Log.w(TAG, "알림 저장 실패 (Gson 직렬화 오류): " + e.getMessage(), e);
                editor.remove(NOTIFICATIONS_KEY);
            }
        }
        editor.apply();
    }

    private void notifyWidgetDataChanged() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, GitHubWidgetProvider.class));

        if (appWidgetIds != null && appWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lvNotifications);
        } else {
            Log.w(TAG, "데이터 변경 알림을 보낼 위젯이 없음");
        }
    }

    /**
     * 기본 RemoteViews를 생성하고 PendingIntent를 설정합니다.
     * 모든 위젯 업데이트에서 공통으로 사용되는 기본 설정을 포함합니다.
     * 
     * @param appWidgetId 위젯 ID (고유한 requestCode 생성용)
     * @return 기본 설정이 완료된 RemoteViews 객체
     */
    private RemoteViews createBaseRemoteViews(int appWidgetId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.github_widget_layout);

        try {
            // 앱 실행 PendingIntent
            Intent appLaunchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (appLaunchIntent != null) {
                PendingIntent appPendingIntent = PendingIntent.getActivity(
                        this,
                        appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_APP_LAUNCH,
                        appLaunchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.tvWidgetTitle, appPendingIntent);
            } else {
                Log.w(TAG, "앱 실행 Intent를 찾을 수 없음");
            }

            // 새로고침 PendingIntent
            Intent refreshIntent = new Intent(this, NotificationService.class);
            PendingIntent refreshPendingIntent = PendingIntent.getService(
                    this,
                    appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_REFRESH,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);

        } catch (Exception e) {
            Log.w(TAG, "PendingIntent 설정 오류: " + e.getMessage(), e);
        }

        return views;
    }

    private void updateWidgetViewVisibility(RemoteViews views, String type) {
        views.setViewVisibility(R.id.pbListLoading, type.equals("loading") ? View.VISIBLE : View.GONE);
        if (type.equals("loading")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.GONE);
        } else if (type.equals("list")) {
            views.setViewVisibility(R.id.lvNotifications, View.VISIBLE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.GONE);
        } else if (type.equals("empty")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.VISIBLE);
            views.setTextViewText(R.id.tvEmptyNotifications, getString(R.string.widget_message_empty));
        } else if (type.equals("error")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.VISIBLE);
            views.setTextViewText(R.id.tvEmptyNotifications, getString(R.string.widget_message_error));
        } else if (type.equals("login_required")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.pbListLoading, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.VISIBLE);
            views.setTextViewText(R.id.tvEmptyNotifications, getString(R.string.widget_message_login_required));
        }

    }
}