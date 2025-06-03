package com.sawaca96.githubwidget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sawaca96.githubwidget.model.Notification;
import com.sawaca96.githubwidget.network.GithubApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GithubClient {
    private static final String GITHUB_API_URL = "https://api.github.com/";
    private final GithubApiService apiService;
    private final String token;

    public GithubClient(String token) {
        this.token = "token " + token;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GITHUB_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.apiService = retrofit.create(GithubApiService.class);
    }

    public List<Notification> fetchNotifications() {
        Call<List<com.sawaca96.githubwidget.network.dto.Notification>> call = apiService.getNotifications(this.token,
                true);

        try {
            Response<List<com.sawaca96.githubwidget.network.dto.Notification>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                List<com.sawaca96.githubwidget.network.dto.Notification> results = response.body();

                List<Notification> notifications = new ArrayList<>();
                for (com.sawaca96.githubwidget.network.dto.Notification value : results) {
                    if (value.getRepository() == null || value.getSubject() == null) {
                        // 필수 정보 누락 시 건너뛰기
                        continue;
                    }

                    String pullRequestId = null;
                    if ("PullRequest".equals(value.getSubject().getType()) && value.getSubject().getUrl() != null) {
                        pullRequestId = extractPullRequestId(value.getSubject().getUrl());
                    }

                    notifications.add(new Notification(
                            value.getId(),
                            value.getSubject().getTitle(),
                            value.getRepository().getName(),
                            value.getRepository().getFullName(),
                            value.getUpdatedAt(),
                            value.getSubject().getType(),
                            pullRequestId,
                            value.getReason()));
                }
                return notifications;
            } else {
                System.err.println("Failed to fetch notifications: " + response.code() + " - " + response.message());
                if (response.errorBody() != null) {
                    try {
                        System.err.println("Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        System.err.println("Error reading error body: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException during fetching notifications: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private String extractPullRequestId(String url) {
        if (url == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(".*/pulls/(\\d+)$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            try {
                return matcher.group(1);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse Pull Request ID: " + url);
            }
        }
        return null;
    }
}