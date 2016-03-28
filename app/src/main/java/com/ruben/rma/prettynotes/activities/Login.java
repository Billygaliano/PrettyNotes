package com.ruben.rma.prettynotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ruben.rma.prettynotes.R;

public class Login extends Activity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);

        final Intent acceso = new Intent(this,MainActivity.class);
        if(isLoggedIn()){
            startActivity(acceso);
        }else{
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>(){

                @Override
                public void onSuccess(LoginResult loginResult) {
                    startActivity(acceso);
                    loginResult.getAccessToken().getPermissions();
                }

                @Override
                public void onCancel() {}

                @Override
                public void onError(FacebookException error) {}
            });
        }

    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
