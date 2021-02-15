package com.example.scheduleapp;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * ユーザの情報を管理するクラス
 */
public class UserStatus extends Object implements Serializable {

    private String userId = null; //ユーザID
    private String password =null; //パスワード
    private String authKey = null; //2段階認証キー
    private SecretKey key = null; //秘密鍵
    private SpinnerItem beforeSpinnerItem ; //過去時間のスピナのアイテム
    private SpinnerItem afterSpinnerItem; //未来時間のスピナのアイテム


    public UserStatus(){
        this.beforeSpinnerItem = SpinnerItem.NOW;
        this.afterSpinnerItem = SpinnerItem.SIX_MONTH_AFTER;
    }
    /**
     * ユーザ情報をを読み込む
     */
    public void readUserStatus(){
        try {
            Gson gson = new Gson();
            this.key = KeyUtility.stringToKey(FileUtility.readFile("key"));
            this.userId = FileUtility.readFileByAES("user_id", this.key);
            this.password = FileUtility.readFileByAES("password", this.key);
            this.authKey = FileUtility.readFileByAES("auth_key", this.key);
            this.beforeSpinnerItem = gson.fromJson(FileUtility.readFileByAES("before_period",this.key),SpinnerItem.class);
            this.afterSpinnerItem = gson.fromJson(FileUtility.readFileByAES("after_period",this.key),SpinnerItem.class);
        }
        catch (IllegalArgumentException anException){
            anException.printStackTrace();
        }
        catch (IOException anException){
            anException.printStackTrace();
        }
        catch (GeneralSecurityException anException){
            anException.printStackTrace();
        }

    }

    /**
     * ユーザ情報を保存
     */
    public void writeUserStatus(){
        try {
            this.key = KeyUtility.generateAESKey();
            Gson gson = new Gson();
            FileUtility.writeFileByAES("user_id",this.userId,this.key);
            FileUtility.writeFileByAES("password",this.password,this.key);
            FileUtility.writeFileByAES("auth_key",this.authKey,this.key);
            FileUtility.writeFileByAES("before_period",gson.toJson(this.beforeSpinnerItem),this.key);
            FileUtility.writeFileByAES("after_period",gson.toJson(this.afterSpinnerItem),this.key);
            FileUtility.writeFile("key",KeyUtility.keyToString(this.key));

        }catch (GeneralSecurityException anException){
            anException.printStackTrace();
        }
        catch (IOException anException){
            anException.printStackTrace();
        }
    }


    /**
     * user_idを設定
     * @param userId ユーザID
     */
    public void setUserId(String userId){
        this.userId = userId;
    }

    /**
     * user_idを返す
     * @return ユーザID
     */
    public String getUserId(){
        return this.userId;
    }

    /**
     * パスワードを設定する
     * @param password パスワード
     */
    public void setPassword(String password){
        this.password = password;
    }

    /**
     * パスワードを返す
     * @return パスワード
     */
    public String getPassword(){
        return this.password;
    }

    /**
     * 2段階認証キーの設定
     * @param authKey 認証キー
     */
    public void setAuthKey(String authKey){
        this.authKey = authKey;
    }

    /**
     * 2段階認証キーを返す
     * @return 2段階認証キー
     */
    public String getAuthKey(){
        return this.authKey;
    }

    /**
     * /過去時間のスピナのインデックス番号をセットする
     * @param spinnerItem
     */
    public void setBeforeSpinnerItem(SpinnerItem spinnerItem){
        this.beforeSpinnerItem = spinnerItem;
    }

    /**
     * /過去時間のスピナのインデックス番号をセットする
     * @return スピナのインデックス番号
     */
    public SpinnerItem getBeforeSpinnerItem(){
        return this.beforeSpinnerItem;
    }

    /**
     * /未来時間のスピナのアイテムをセットする
     * @param spinnerItem スピナアイテム
     */
    public void setAfterSpinnerItem(SpinnerItem spinnerItem){
        this.afterSpinnerItem = spinnerItem;
    }

    /**
     * /未来時間のスピナのインデックス番号をセットする
     * @return スピナのインデックス番号
     */
    public SpinnerItem getAfterSpinnerItem(){
        return this.afterSpinnerItem;
    }

    /**
     * AES秘密鍵の設定
     * @param key 秘密鍵
     */
    public void setKey(SecretKey key){
        this.key = key;
    }

}
