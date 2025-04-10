package com.sawaca96.githubwidget.model;

import android.text.format.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Notification {
    private String id;
    private String title;
    private String updatedAt;
    private String repoName;
    private String repoFullName;
    private String type;
    private String pullRequestId;

    public Notification(
            String id,
            String title,
            String repoName,
            String repoFullName,
            String updatedAt,
            String type,
            String pullRequestId) {
        this.id = id;
        this.title = title;
        this.repoName = repoName;
        this.repoFullName = repoFullName;
        this.updatedAt = updatedAt;
        this.type = type;
        this.pullRequestId = pullRequestId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getType() {
        return type;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public String timeDiff() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date updatedDate = sdf.parse(this.updatedAt);
            if (updatedDate == null) {
                return "";
            }
            long now = Calendar.getInstance().getTimeInMillis();
            return DateUtils.getRelativeTimeSpanString(
                    updatedDate.getTime(),
                    now,
                    DateUtils.MINUTE_IN_MILLIS)
                    .toString();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUrl() {
        if (this.pullRequestId == null) {
            return "https://github.com/" + this.repoFullName;
        }
        return "https://github.com/" + this.repoFullName + "/pull/" + this.pullRequestId;
    }
}