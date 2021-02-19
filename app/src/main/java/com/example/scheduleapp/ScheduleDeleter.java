package com.example.scheduleapp;

import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;

public class ScheduleDeleter {

    private SubjectFragmentModel model;
    private WebView hiddenView; //WebView
    private UserStatus user; //ユーザを管理するクラス
    private CookieManager cookieManager; // Cookie管理クラス
    private Integer accessErrorCount = 0; // アクセス不能回数をカウントする変数
    private Subject deleteSubject;

    public ScheduleDeleter(SubjectFragmentModel model,WebView aWebView) {
        this.hiddenView = aWebView;
        this.model = model;
        this.hiddenView.getSettings().setJavaScriptEnabled(true);//javascriptオン
        this.hiddenView.getSettings().setDomStorageEnabled(true); // WebStorageをオン
        this.hiddenView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        this.hiddenView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.hiddenView.clearCache(true);
        this.cookieManager= CookieManager.getInstance();
        this.cookieManager.setAcceptCookie(true);
        this.cookieManager.setAcceptThirdPartyCookies(this.hiddenView, true);
        this.cookieManager.flush();
        this.hiddenView.setWebViewClient(new moodleWebViewClient());
        this.user = new UserStatus();
    }


    /**
     * moodleログインのためのWebViewClient
     */
    private class moodleWebViewClient extends WebViewClient {
        private Integer loginErrorCount = 0; //ログインエラーの回数をカウントする変数

        /**
         * ページ読み込み完了後の処理
         *
         * @param view WebView
         * @param url  読み込んだURL
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            System.out.println(url);
            if (this.loginErrorCount > 10) {
                System.out.println("ログインエラー");
                accessErrorCount = 0;
                this.loginErrorCount = 0;
                model.notifyFailedDeleteEvent("ログインできませんでした");
                return;
            }
            //Moodleなら
            if (url.matches("https://cclms.kyoto-su.ac.jp/")) {
                System.out.println("delete schedule");
                executeDeleteCEvent();
            }
            //ログインページなら
            else if (url.matches("https://gakunin.kyoto-su.ac.jp/idp/profile/SAML2/Redirect/SSO.execution=.*")) {
                this.loginErrorCount += 1;
                view.evaluateJavascript("document.getElementById('username')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        user.readUserStatus();
                        if (!value.equals("null")) { //1段階目の認証
                            System.out.println("first");
                            view.evaluateJavascript("document.getElementById('username').value='" + user.getUserId() + "'", null);
                            view.evaluateJavascript("document.getElementById('password').value='" + user.getPassword() + "'", null);
                            view.evaluateJavascript("var elements=document.getElementsByClassName('form-element form-button')\nelements[0].click()", null);
                        } else { //2段階目の認証
                            System.out.println("second");
                            try {
                                String script = String.format("document.getElementById('token').value='%s'", AuthPassWord.getAuthPass(user.getAuthKey()));
                                view.evaluateJavascript(script, null);
                                view.evaluateJavascript("var elements=document.getElementsByClassName('form-element form-button')\nelements[0].click()", null);
                            } catch (Exception anException) {
                                anException.printStackTrace();
                            }
                        }
                    }
                });
            } else if (url.matches("https://cclms.kyoto-su.ac.jp/login/index.php?")) {
                gakuninButtonClick();
            }
        }

        /**
         * 読み込みエラーの場合の処理
         * @param view WebView
         * @param request リクエスト
         * @param error エラー
         */
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request , WebResourceError error) {
            if(accessErrorCount>10){
                System.out.println("アクセスエラー");
                accessErrorCount = 0;
                this.loginErrorCount = 0;
                model.notifyFailedDeleteEvent("Moodleにアクセス出来ませんでした");
            }
            else if(request.isForMainFrame()) {
                failedToAccess();
            }
            return;
        }
    }

    /**
     * 作成に成功した時の処理
     */
    public void successDelete(){
        this.model.notifySuccessDelete();
    }

    /**
     * アクセスに失敗した場合の処理
     * エラーカウントを1増加させ、再度Moodleにアクセスする
     */
    public void failedToAccess(){
        this.accessErrorCount+=1;
        this.loadMoodle();
    }

    /**
     * Moodleにアクセスする
     */
    public void loadMoodle(){
        this.user.readUserStatus();
        this.hiddenView.loadUrl("https://cclms.kyoto-su.ac.jp/auth/shibboleth/");
    }

    public void deleteCalendarEvent(Subject aSubject){
        this.loadMoodle();
        this.deleteSubject = aSubject;
    }

    /**
     * カレンダーイベントを作成するJSを実行する
     */
    private void executeDeleteCEvent(){
        try {
            Integer id = this.deleteSubject.getId();
            String script = FileUtility.readAssets("deleteCalendarEvent.js");
            script+="(function () { wait("+id+"); })();";
            this.hiddenView.evaluateJavascript(script, null);
        }catch (IOException anException){
            anException.printStackTrace();
        }
    }

    /**
     * 学認からログインのボタンをクリックする
     */
    private void gakuninButtonClick(){
        try {
            String script = FileUtility.readAssets("gakuninButtonClick.js");
            this.hiddenView.evaluateJavascript(script, null);
        }catch (IOException anException){
            anException.printStackTrace();
            System.out.println("js読み込み失敗");
        }
        return;
    }
}
