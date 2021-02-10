package com.example.scheduleapp;


import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import java.io.IOException;

public class ScheduleGetter extends Object{
    private  WebView hiddenView;
    private AuthPassWord anAuthPassWord;

    public ScheduleGetter(Model model,WebView aView){
        this.hiddenView = aView;
        this.hiddenView.getSettings().setJavaScriptEnabled(true);//javascriptオン
        this.hiddenView.getSettings().setDomStorageEnabled(true); // WebStorageをオン
        this.hiddenView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        this.hiddenView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.hiddenView.clearCache(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookies(null);
        cookieManager.getInstance().flush();
        cookieManager.setAcceptThirdPartyCookies(this.hiddenView, true);
        anAuthPassWord = new AuthPassWord();
    }

    /**
     * Moodleにログインする
     */
    public void loadMoodle(){
        UserStatus user = new UserStatus();
        user.readUserStatus();
        this.hiddenView.loadUrl("https://cclms.kyoto-su.ac.jp/auth/shibboleth/");
        this.hiddenView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view,url);
                int len = url.length(); //urlの長さ
                char end = url.charAt(len - 1);
                System.out.println(url);
                if(url.matches("https://cclms.kyoto-su.ac.jp/")){
                    System.out.println("get schedule");
                    getCalendarEvents();
                }
                else if (end == '1') {
                    view.evaluateJavascript("document.getElementById('username').value='"+user.getUserId()+"'", null);
                    view.evaluateJavascript("document.getElementById('password').value='"+user.getPassword()+"'", null);
                    view.evaluateJavascript("var elements=document.getElementsByClassName('form-element form-button')\nelements[0].click()", null);
                } else if (end == '2') {
                    try {
                        String script = String.format("document.getElementById('token').value='%s'", anAuthPassWord.getAuthPass(user.getAuthKey()));
                        view.evaluateJavascript(script, null);
                        view.evaluateJavascript("var elements=document.getElementsByClassName('form-element form-button')\nelements[0].click()", null);
                    } catch (Exception anException) {
                        anException.printStackTrace();
                    }
                }else if(url.matches("https://cclms.kyoto-su.ac.jp/login/index.php?")){
                    gakuninButtonClick();
                }
             }

            @Override
            public void onReceivedError(WebView webview, int errorCode, String description, String failingUrl) {
                System.out.println(errorCode);
                if(errorCode < 0){
                    hiddenView.loadUrl("https://cclms.kyoto-su.ac.jp/auth/shibboleth/");
                }
            }
        });

    }

    /**
     * カレンダーからイベントIDを取得し、イベントの詳細を取得するJavascriptを実行する
     */
    private void getCalendarEvents(){
        try {
            String script = FileUtility.readAssets("getCalendarSchedule.js");
            this.hiddenView.evaluateJavascript(script, null);
        }
        catch (IOException anException){
            anException.printStackTrace();
            System.out.println("js読み込み失敗");
        }
        return;
    }

    /**
     * 学認からログインのボタンをクリックする
     */
    private void gakuninButtonClick(){
        try {
            String script = FileUtility.readAssets("gakuninButtonClick.js");
            script = script.replaceAll(" \t","");
            this.hiddenView.evaluateJavascript(script, null);
        }catch (IOException anException){
            anException.printStackTrace();
            System.out.println("js読み込み失敗");
        }
        return;
    }

}
