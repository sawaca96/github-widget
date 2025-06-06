package com.sawaca96.githubwidget;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class WidgetUpdateScheduler {
    public static void scheduleUpdate(Context context) {
        // 인터넷 연결 제약 조건 설정
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        // 일회성 작업 요청 생성
        OneTimeWorkRequest updateWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setConstraints(constraints)
                .build();
        // 고유한 작업으로 예약하여 중복 실행 방지
        WorkManager.getInstance(context).enqueueUniqueWork(
                GithubWidgetConstant.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                updateWorkRequest);
    }
}