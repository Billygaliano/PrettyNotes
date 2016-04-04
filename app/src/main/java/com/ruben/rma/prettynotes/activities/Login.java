package com.ruben.rma.prettynotes.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ruben.rma.prettynotes.R;
import com.ruben.rma.prettynotes.connectionws.PostHttp;
import com.ruben.rma.prettynotes.data.NoteBD;
import com.ruben.rma.prettynotes.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends Activity {

    private CallbackManager callbackManager;
    private NoteBD DB;
    LoginButton loginButton;
    ProfileTracker profileTracker;
    Intent acceso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        DB =new NoteBD(this);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        acceso = new Intent(this,MainActivity.class);
        final Context context = this;

        if(isLoggedIn()){
            if(Profile.getCurrentProfile() == null) {
                ProfileTracker profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        String emailUser = currentProfile.getId();
                        acceso.putExtra("email", emailUser);
                        startActivity(acceso);
                    }
                };
                profileTracker.startTracking();
            }else{
                String emailUser = Profile.getCurrentProfile().getId();
                acceso.putExtra("email", emailUser);
                startActivity(acceso);
            }
        }else{
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){
                @Override
                public void onSuccess(final LoginResult loginResult) {
                    if(Profile.getCurrentProfile() == null){
                        profileTracker = new ProfileTracker(){
                            @Override
                            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                                User user = new User();

                                user.setFacebookUserID(currentProfile.getId());
                                acceso.putExtra("email", user.getFacebookUserID());
                                Cursor c = DB.getUserNote(user.getFacebookUserID());
                                if(!c.moveToFirst()){
                                    try{
                                        JSONObject userParam = new JSONObject();
                                        userParam.put("email",user.getFacebookUserID());
                                        new PostHttp(context).execute("" +
                                                        "http://192.168.1.127:8080/PrettyNotesWS/webresources/entity.usernotes?",
                                                userParam.toString());

                                    }catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                startActivity(acceso);
                                loginResult.getAccessToken().getPermissions();
                                profileTracker.stopTracking();
                            }
                        };
                        profileTracker.startTracking();

                    }else{
                        String emailUser = Profile.getCurrentProfile().getId();
                        acceso.putExtra("email", emailUser);
                        startActivity(acceso);
                        loginResult.getAccessToken().getPermissions();
                    }
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
