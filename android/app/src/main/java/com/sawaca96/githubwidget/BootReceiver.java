package com.sawaca96.githubwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

/**
 * 부팅 완료 및 앱 업데이트 시 GitHub 위젯 서비스를 자동으로 시작하는 클래스
 * 
 * 1. 기기 부팅 완료 시 자동으로 서비스 시작 (ACTION_BOOT_COMPLETED)
 * 2. 앱 업데이트 후 서비스 재시작 (ACTION_MY_PACKAGE_REPLACED)
 * 3. 기기 재부팅이나 앱 업데이트와 같은 시스템 이벤트 후에도 GitHub 위젯 관련 서비스가
 * 계속 작동하도록 보장
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    /**
     * 시스템 브로드캐스트 인텐트를 수신하여 서비스를 시작합니다.
     * 
     * @param context 애플리케이션 컨텍스트
     * @param intent  수신된 브로드캐스트 인텐트
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {

            try {
                Intent serviceIntent = new Intent(context, NotificationService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            } catch (Exception e) {
                Log.e(TAG, "서비스 시작 오류: " + e.getMessage());
            }
        }
    }
}