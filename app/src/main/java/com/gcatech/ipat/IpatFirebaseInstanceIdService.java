package com.gcatech.ipat;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utils.PreferenceUtils;

/**
 * Created by jjoya on 13/4/2017.
 */

public class IpatFirebaseInstanceIdService extends FirebaseInstanceIdService {

    public  static String urlImageNotificacion;
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        PreferenceUtils preferenceUtils = new PreferenceUtils(getApplicationContext());
        preferenceUtils.AddToPreferences("tokenNotificationPush", token);
    }
}
