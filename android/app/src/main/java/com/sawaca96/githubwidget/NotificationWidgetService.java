package com.sawaca96.githubwidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sawaca96.githubwidget.model.Notification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class NotificationWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NotificationRemoteViewsFactory(this.getApplicationContext());
    }
}

class NotificationRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "NotificationWidgetSvc";
    private Context context;
    private List<Notification> notifications = new ArrayList<>();
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String NOTIFICATIONS_KEY = GithubWidgetConstant.NOTIFICATIONS_KEY;

    public NotificationRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        // 데이터 로드 등 초기화 작업은 onDataSetChanged에서 수행
    }

    @Override
    public void onDataSetChanged() {
        notifications.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notificationsJsonString = prefs.getString(NOTIFICATIONS_KEY, null);

        if (notificationsJsonString != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<List<Notification>> typeToken = new com.google.gson.reflect.TypeToken<List<Notification>>() {
                };

                List<Notification> loadedNotifications = gson.fromJson(notificationsJsonString, typeToken.getType());
                if (loadedNotifications != null) {
                    notifications.addAll(loadedNotifications);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing notifications from SharedPreferences: " + e.getMessage());
                notifications.clear();
            }
        }
    }

    @Override
    public void onDestroy() {
        notifications.clear();
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    /**
     * 지정된 위치의 항목에 대한 RemoteViews를 생성합니다.
     * 
     * 이 메서드는 시스템에 의해 자동으로 호출되며, 위젯의 ListView가 화면에 그려질 때
     * 각 항목마다 호출됩니다. ListView의 스크롤에 따라 필요한 항목의 뷰를 생성합니다.
     * 
     * notifyAppWidgetViewDataChanged()가 호출되면 시스템이 데이터를 다시 요청하면서
     * 이 메서드를 다시 호출하여 위젯 항목을 새로고침합니다.
     *
     * @param position 생성할 항목의 위치 (0부터 시작)
     * @return 해당 위치의 항목에 대한 RemoteViews 객체
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= notifications.size()) {
            return null;
        }

        Notification notification = notifications.get(position);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.github_notification_item);

        String repoName = notification.getRepoName();
        String prNumberString = "";
        if (notification.getPullRequestId() != null && !notification.getPullRequestId().isEmpty()) {
            prNumberString = " #" + notification.getPullRequestId();
        }

        // PR번호에만 특정 스타일 적용
        SpannableString repoNameWithPrSpannable = new SpannableString(repoName + prNumberString);
        if (!prNumberString.isEmpty()) {
            int prNumberColor = android.graphics.Color.rgb(0x8B, 0x94, 0x9E);
            repoNameWithPrSpannable.setSpan(
                    new android.text.style.ForegroundColorSpan(prNumberColor),
                    repoName.length(),
                    repoName.length() + prNumberString.length(),
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        rv.setTextViewText(R.id.widgetNotificationRepoName, repoNameWithPrSpannable);
        rv.setTextViewText(R.id.widgetNotificationTitle, notification.getTitle());
        rv.setTextViewText(R.id.widgetNotificationUpdatedAt, notification.timeDiff());
        rv.setTextViewText(R.id.widgetNotificationStatus, notification.getReason());
        setIcon(notification.getType(), rv);
        return rv;
    }

    private void setIcon(String type, RemoteViews rv) {
        int iconResId = R.drawable.ic_octicon_question;
        int iconTintColor = android.graphics.Color.parseColor("#8B949E");

        if (type != null) {
            switch (type) {
                case "CheckSuite":
                    iconResId = R.drawable.ic_octicon_sync;
                    iconTintColor = android.graphics.Color.parseColor("#2DA44E");
                    break;
                case "Commit":
                    iconResId = R.drawable.ic_octicon_git_commit;
                    break;
                case "Discussion":
                    iconResId = R.drawable.ic_octicon_comment_discussion;
                    break;
                case "Issue":
                    iconResId = R.drawable.ic_octicon_issue_opened;
                    iconTintColor = android.graphics.Color.parseColor("#2DA44E");
                    break;
                case "PullRequest":
                    iconResId = R.drawable.ic_octicon_git_pull_request;
                    iconTintColor = android.graphics.Color.parseColor("#2DA44E");
                    break;
                case "Release":
                    iconResId = R.drawable.ic_octicon_tag;
                    break;
                case "RepositoryVulnerabilityAlert":
                    iconResId = R.drawable.ic_octicon_alert;
                    iconTintColor = android.graphics.Color.parseColor("#D73A49");
                    break;
            }
        }
        rv.setImageViewResource(R.id.widgetNotificationIcon, iconResId);
        rv.setInt(R.id.widgetNotificationIcon, "setColorFilter", iconTintColor);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // 단일 종류의 아이템 뷰 사용
    }

    /**
     * 지정된 위치 항목의 고유 ID를 반환합니다.
     * 
     * 이 메서드는 시스템이 위젯 항목을 식별하고 효율적으로 갱신하는 데 사용됩니다.
     * hasStableIds()가 true를 반환할 경우 같은 항목은 항상 동일한 ID를 반환해야 합니다.
     * 
     * 반환되는 ID는 항목의 고유성을 보장해야 하며, 같은 항목이라도 다른 데이터로 변경된 경우
     * UI가 적절히 갱신되도록 해야 합니다.
     *
     * @param position 항목의 위치 (0부터 시작)
     * @return 해당 위치 항목의 고유 ID
     */
    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < notifications.size()) {
            String id = notifications.get(position).getId();
            if (id != null) {
                try {
                    // GitHub 알림 ID가 숫자 형식이면 직접 변환
                    return Long.parseLong(id);
                } catch (NumberFormatException e) {
                    // 숫자가 아닌 경우 또는 long 범위를 초과하는 경우
                    // String.hashCode()는 int를 반환하므로 부호 비트를 처리하여 변환
                    return id.hashCode() & 0xFFFFFFFFL; // 음수 방지를 위해 부호 없는 변환
                }
            }
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true; // getItemId가 안정적인 ID를 반환한다고 가정
    }
}