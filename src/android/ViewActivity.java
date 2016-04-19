package com.datum.hotline.plugin.hlpush.localnotification;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Raziff on 19/04/2016.
 */
public class ViewActivity extends Activity {
    @Override
    public void onCreate (Bundle state) {
        super.onCreate(state);
        PackageManager pm = getPackageManager();
        String pkgName = getPackageName();

        Intent i = getIntent();
        String data = i.getDataString();
        SharedPreferences sharedPref = getSharedPreferences(pkgName,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hotlines", data);
        Boolean a = editor.commit();

        Intent intent = pm.getLaunchIntentForPackage(pkgName);

        startActivity(intent);
    }

    @Override
    public void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        PackageManager pm = getPackageManager();
        String pkgName = getPackageName();

        Log.d("ViewActivity","onNewIntent");
        String data = intent.getDataString();
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hotlines", data);
        editor.commit();

        Intent i = pm.getLaunchIntentForPackage(pkgName);

        startActivity(i);
    }
}
