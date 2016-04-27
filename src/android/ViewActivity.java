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
        Log.d("ViewActivity","onCreate=>pkgName: "+pkgName);
        Log.d("ViewActivity","onCreate=>data: "+data);

        SharedPreferences sharedPref = getSharedPreferences(pkgName,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hotlines", data);
        Boolean a = editor.commit();

        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        //intent.putExtra("hotlines", data);
        startActivity(intent);
    }

    @Override
    public void onNewIntent (Intent i) {
        super.onNewIntent(i);
        PackageManager pm = getPackageManager();
        String pkgName = getPackageName();

        String data = i.getDataString();

        Log.d("ViewActivity","onCreate=>pkgName: "+pkgName);
        Log.d("ViewActivity","onCreate=>data: "+data);
        if(data != null && !data.isEmpty()){
            SharedPreferences sharedPref = getSharedPreferences(pkgName,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("hotlines", data);
            editor.commit();
        }else{
            Log.d("ViewActivity","onCreate=>data is empty ");
        }

        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        //intent.putExtra("hotlines", data);
        startActivity(intent);
    }
}
