package com.sawaca96.githubwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.app.PendingIntent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sawaca96.githubwidget.model.Notification;

import java.util.List;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String TOKEN_KEY = GithubWidgetConstant.TOKEN_KEY;
    private static final String USERNAME_KEY = GithubWidgetConstant.USERNAME_KEY;
    private static final String NOTIFICATIONS_KEY = GithubWidgetConstant.NOTIFICATIONS_KEY;

    private final Context context;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, GitHubWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        if (appWidgetIds == null || appWidgetIds.length == 0) {
            Log.w(TAG, "업데이트할 위젯이 없음");
            return Result.success();
        }

        mainHandler.post(() -> {
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = createBaseRemoteViews(appWidgetId);
                updateWidgetViewVisibility(views, "loading");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final String token = prefs.getString(TOKEN_KEY, "");
        final String username = prefs.getString(USERNAME_KEY, "");

        if (token.isEmpty() || username.isEmpty()) {
            mainHandler.post(() -> {
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = createBaseRemoteViews(appWidgetId);
                    updateWidgetViewVisibility(views, "login_required");

                    Intent loginIntent = new Intent(context, MainActivity.class);
                    loginIntent.putExtra("navigate_to_login", true);
                    PendingIntent loginPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_LOGIN,
                            loginIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.tvEmptyNotifications, loginPendingIntent);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            });
            return Result.success();
        }

        GithubClient githubClient = new GithubClient(token);
        List<Notification> notifications;
        try {
            notifications = githubClient.fetchNotifications();
            saveNotificationsToPrefs(notifications);

            List<Notification> finalNotifications = notifications;
            mainHandler.post(() -> {
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = createBaseRemoteViews(appWidgetId);
                    if (finalNotifications.isEmpty()) {
                        updateWidgetViewVisibility(views, "empty");
                    } else {
                        Intent remoteAdapterIntent = new Intent(context, NotificationRemoteViewsService.class);
                        views.setRemoteAdapter(R.id.lvNotifications, remoteAdapterIntent);

                        Intent clickIntentTemplate = new Intent(Intent.ACTION_VIEW);

                        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            // 암시적 인텐트에서는 FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT 추가 필요
                            flags |= PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;
                        }

                        PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(
                                context,
                                appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_ITEM_CLICK,
                                clickIntentTemplate,
                                flags);
                        views.setPendingIntentTemplate(R.id.lvNotifications, clickPendingIntentTemplate);

                        updateWidgetViewVisibility(views, "list");
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

                if (!finalNotifications.isEmpty()) {
                    notifyWidgetDataChanged();
                }
            });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "알림 가져오기 실패: ", e);
            mainHandler.post(() -> {
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = createBaseRemoteViews(appWidgetId);
                    updateWidgetViewVisibility(views, "error");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            });
            return Result.failure();
        }
    }

    private void saveNotificationsToPrefs(List<Notification> notifications) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, GitHubWidgetProvider.class));

        if (appWidgetIds != null && appWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lvNotifications);
        } else {
            Log.w(TAG, "데이터 변경 알림을 보낼 위젯이 없음");
        }
    }

    private RemoteViews createBaseRemoteViews(int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.github_widget_layout);

        try {
            Intent appLaunchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (appLaunchIntent != null) {
                PendingIntent appPendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId * 100 + GithubWidgetConstant.PENDING_INTENT_APP_LAUNCH,
                        appLaunchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.tvWidgetTitle, appPendingIntent);
            } else {
                Log.w(TAG, "앱 실행 Intent를 찾을 수 없음");
            }

            Intent refreshIntent = new Intent(context, GitHubWidgetProvider.class);
            refreshIntent.setAction(GithubWidgetConstant.ACTION_REFRESH);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
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
            views.setTextViewText(R.id.tvEmptyNotifications, context.getString(R.string.widget_message_empty));
        } else if (type.equals("error")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.VISIBLE);
            views.setTextViewText(R.id.tvEmptyNotifications, context.getString(R.string.widget_message_error));
        } else if (type.equals("login_required")) {
            views.setViewVisibility(R.id.lvNotifications, View.GONE);
            views.setViewVisibility(R.id.pbListLoading, View.GONE);
            views.setViewVisibility(R.id.tvEmptyNotifications, View.VISIBLE);
            views.setTextViewText(R.id.tvEmptyNotifications, context.getString(R.string.widget_message_login_required));
        }
    }
}