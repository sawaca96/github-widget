package com.sawaca96.githubwidget.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * UI 관련 유틸리티 메서드를 제공하는 클래스
 */
public class UIUtils {
    private static final String TAG = "UIUtils";
    private static Handler mainHandler;

    /**
     * Toast 메시지를 안전하게 표시합니다.
     *
     * @param context      컨텍스트
     * @param message      표시할 메시지
     * @param longDuration true면 LENGTH_LONG, false면 LENGTH_SHORT 사용
     */
    public static void showToast(Context context, String message, boolean longDuration) {
        try {
            if (context == null)
                return;

            // 메인 스레드에서 실행 보장
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(context, message,
                        longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            } else {
                if (mainHandler == null) {
                    mainHandler = new Handler(Looper.getMainLooper());
                }
                mainHandler.post(() -> Toast.makeText(context, message,
                        longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            Log.e(TAG, "토스트 표시 오류: " + e.getMessage(), e);
        }
    }

    /**
     * Toast 메시지를 짧은 시간 동안 표시합니다.
     *
     * @param context 컨텍스트
     * @param message 표시할 메시지
     */
    public static void showShortToast(Context context, String message) {
        showToast(context, message, false);
    }

    /**
     * Toast 메시지를 긴 시간 동안 표시합니다.
     *
     * @param context 컨텍스트
     * @param message 표시할 메시지
     */
    public static void showLongToast(Context context, String message) {
        showToast(context, message, true);
    }
}