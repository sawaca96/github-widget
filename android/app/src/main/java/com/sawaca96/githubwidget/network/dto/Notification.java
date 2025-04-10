package com.sawaca96.githubwidget.network.dto;

import com.google.gson.annotations.SerializedName;

import com.sawaca96.githubwidget.network.dto.Repository;
import com.sawaca96.githubwidget.network.dto.Subject;

public class Notification {
    private String id;
    private Repository repository;
    private Subject subject;
    @SerializedName("updated_at")
    private String updatedAt;
    private String reason;
    private boolean unread;

    public String getId() {
        return id;
    }

    public Repository getRepository() {
        return repository;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getReason() {
        return reason;
    }

    public boolean isUnread() {
        return unread;
    }
}