package com.sawaca96.githubwidget.network.dto;

import com.google.gson.annotations.SerializedName;

public class Repository {
    private String name;
    @SerializedName("full_name")
    private String fullName;

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }
}