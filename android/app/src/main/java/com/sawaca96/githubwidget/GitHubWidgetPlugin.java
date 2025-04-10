package com.sawaca96.githubwidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.sawaca96.githubwidget.GithubWidgetConstant;

@CapacitorPlugin(name = "GitHubWidget")
public class GitHubWidgetPlugin extends Plugin {
    private static final String TAG = "GitHubWidgetPlugin";
    private static final String PREFS_NAME = GithubWidgetConstant.PREFS_NAME;
    private static final String TOKEN_KEY = GithubWidgetConstant.TOKEN_KEY;
    private static final String USER_ID_KEY = GithubWidgetConstant.USER_ID_KEY;
    private static final String USERNAME_KEY = GithubWidgetConstant.USERNAME_KEY;
    
    @PluginMethod
    public void saveGitHubToken(PluginCall call) {
        String token = call.getString("token");
        String userId = call.getString("userId");
        String username = call.getString("username");
        if (token == null) {
            call.reject("토큰 값이 필요합니다");
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString(TOKEN_KEY, token);
        
        if (userId != null) {
            editor.putString(USER_ID_KEY, userId);
        }
        
        if (username != null) {
            editor.putString(USERNAME_KEY, username);
        }
        
        editor.apply();
        
        JSObject ret = new JSObject();
        ret.put("success", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void getGitHubToken(PluginCall call) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        String userId = prefs.getString(USER_ID_KEY, null);
        String username = prefs.getString(USERNAME_KEY, null);
        
        JSObject ret = new JSObject();
        ret.put("token", token);
        ret.put("userId", userId);
        ret.put("username", username);
        
        call.resolve(ret);
    }

    @PluginMethod
    public void clearGitHubToken(PluginCall call) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(USER_ID_KEY);
        editor.remove(USERNAME_KEY);
        editor.apply();
        
        JSObject ret = new JSObject();
        ret.put("success", true);
        call.resolve(ret);
    }
} 