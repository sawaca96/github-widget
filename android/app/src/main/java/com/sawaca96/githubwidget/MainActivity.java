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
    }
}
