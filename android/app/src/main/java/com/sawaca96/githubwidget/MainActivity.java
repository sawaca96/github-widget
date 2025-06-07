package com.sawaca96.githubwidget;

import android.os.Bundle;
import android.util.Log;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(GitHubWidgetPlugin.class);
        super.onCreate(savedInstanceState);
        // 앱 업데이트 시 MainActivity가 실행되면서 위젯을 갱신합니다.
        // BootReceiver에서는 앱이 활성화 되는 시간과의 레이스컨디션이 발생
        WidgetUpdateScheduler.scheduleUpdate(this);
    }
}
