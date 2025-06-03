package com.sawaca96.githubwidget.network;

import com.sawaca96.githubwidget.network.dto.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface GithubApiService {
    @GET("notifications")
    Call<List<Notification>> getNotifications(
            @Header("Authorization") String token,
            @Query("all") boolean all);
}