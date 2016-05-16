package com.plugin.gcm;

import com.google.android.gcm.GCMBaseIntentService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import com.datum.hotline.plugin.hlpush.localnotification.TriggerReceiver;
import com.datum.hotline.plugin.hlpush.notification.Manager;
import com.datum.hotline.plugin.hlpush.localnotification.LocalNotification;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

    //public static final int NOTIFICATION_ID = 237;
    public static final int NOTIFICATION_ID = 1;

    private static String LOGTAG = "LN-GCMIntentService";
    static final String TAG = "HotlineNotifications";
    private static final String STORAGE_FOLDER = "/localnotification";

    //public static final String MESSAGE = "message";
    public static final String MESSAGE = "msg";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    public void onRegistered(Context context, String regId) {
        Log.d(LOGTAG, "onRegistered: " + regId);
        NotificationService.getInstance(context).onRegistered(regId);
    }

    @Override
    public void onUnregistered(Context context, String regId) {
        Log.d(LOGTAG, "onUnregistered - regId: " + regId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        boolean isAppInForeground = NotificationService.getInstance(context).isForeground();

        Bundle extras = intent.getExtras();
        if (extras != null) {

            // If in background, create notification to display in notification center
            if (!isAppInForeground) {
                /*if (extras.getString(MESSAGE) != null && extras.getString(MESSAGE).length() != 0) {
                    createNotification(context, extras);
                }*/
                //createNotification(context, extras);
                Log.d(LOGTAG, "onMessage: " + extras.toString());

                String msg = "";
                String data = extras.getString("data");

                if(data != null){
                    try {
                        JSONObject t = new JSONObject(data);
                        msg = t.getString("msg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //if (extras.getString(MESSAGE) != null && extras.getString(MESSAGE).length() != 0){
                //if (data != null && data.getString(MESSAGE) != null && data.getString(MESSAGE).length() != 0){
                if (data != null && msg != null && msg.length() != 0){
                    JSONObject appends = ToJson(extras);
                    try {
                        appends.put("id",1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                    String sound = appends.optString("sound", "");
                    /*try {
                        //String pkg = context.getPackageName();
                        String ExFilesDir = context.getExternalFilesDir(null).getAbsolutePath();
                        Log.d(LOGTAG, "getExternalFilesDir path: "+ ExFilesDir);
                        if(!sound.isEmpty()){
                            File soundfile = new File(ExFilesDir, sound);
                            Log.d(LOGTAG, "soundfile path: "+ soundfile.toString());
                            //Log.d(LOGTAG, "soundfile exist: "+ soundfile.exists());
                            String joinedPath = soundfile.toString();
                            //sound = "file://"+joinedPath;
                            sound = "file:///android_asset/steroids/build/audio/s01.mp3";
                            appends.putOpt("sound",sound);
                        }

                    } catch (Exception e) {
                        Log.e(LOGTAG, "Error prepend sound name with getExternalFilesDir");
                        e.printStackTrace();
                    }*/
                    try {
                        //String pkg = context.getPackageName();
                        if(!sound.isEmpty()){
                            sound = getPathFromAsset(context,sound);
                            appends.putOpt("sound",sound);
                        }else{
                            Log.d(LOGTAG, "push notification sound path: soundpath was empty");
                        }

                    } catch (Exception e) {
                        Log.e(LOGTAG, "Error prepend sound name with getExternalFilesDir");
                        e.printStackTrace();
                    }
                    Log.d(LOGTAG, "push notification sound path: "+ sound);

                    String icon = appends.optString("icon", "");
                    Log.d(LOGTAG, "Icon string: "+ icon);
                    if(icon == ""){
                        icon = "assets://steroids/build/icons/icon.png";
                    }
                    /*try {
                        //String pkg = context.getPackageName();
                        String ExFilesDir = context.getExternalFilesDir(null).getAbsolutePath();
                        //Log.d(LOGTAG, "getExternalFilesDir path: "+ ExFilesDir);
                        if(!icon.isEmpty()){
                            String joinedPath = new File(ExFilesDir, icon).toString();
                            icon = "file://"+joinedPath;
                            appends.putOpt("icon",icon);
                            Log.d(LOGTAG, "push notification icon path: "+ icon);
                        }else{
                            Log.d(LOGTAG, "push notification icon path: iconpath was empty");
                        }

                    } catch (Exception e) {
                        Log.e(LOGTAG, "Error prepend icon name with getExternalFilesDir");
                        e.printStackTrace();
                    }*/
                    try {
                        //String pkg = context.getPackageName();
                        if(!icon.isEmpty()){
                            icon = getPathFromAsset(context,icon);
                            appends.putOpt("icon",icon);
                        }else{
                            Log.d(LOGTAG, "push notification icon path: iconpath was empty");
                        }

                    } catch (Exception e) {
                        Log.e(LOGTAG, "Error prepend sound name with getExternalFilesDir");
                        e.printStackTrace();
                    }
                    Log.d(LOGTAG, "push notification icon path: "+ icon);

                    try{
                        Context baseContext = getBaseContext();
                        boolean success = LocalNotification.incBadge(baseContext);
                        Log.d(LOGTAG, "push notification=>increment badge:"+ Boolean.valueOf(success).toString());
                    }catch (Exception e){
                        Log.e(LOGTAG, "push notification=>increment=>error");
                        e.printStackTrace();
                    }

                    Manager.getInstance(context).append(1, appends,TriggerReceiver.class);
                }
            }

            NotificationService.getInstance(context).onMessage(extras);
        }
    }

    private String getPathFromAsset(Context context, String path) {
        File dir = context.getExternalCacheDir();

        if (dir == null) {
            Log.e("Asset", "Missing external cache dir");
            return "";
        }
        String resPath  = path.replaceFirst("assets://", "");
        String fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        String storage  = dir.toString() + STORAGE_FOLDER;
        File file       = new File(storage, fileName);

        //noinspection ResultOfMethodCallIgnored
        new File(storage).mkdir();

        if(!file.exists()){
            try {
                AssetManager assets = context.getAssets();
                FileOutputStream outStream = new FileOutputStream(file);
                InputStream inputStream = assets.open(resPath);

                copyFile(inputStream, outStream);

                outStream.flush();
                outStream.close();

                //return Uri.fromFile(file);
                return "file://"+file.toString();

            } catch (Exception e) {
                Log.e("Asset", "File not found: assets/" + resPath);
                e.printStackTrace();
            }
        }else{
            return "file://"+file.toString();
        }


        return "";
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private JSONObject ToJson(Bundle bundle){
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                //Handle exception here
            }
        }
        return json;
    }

    public void createNotification(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = Notification.DEFAULT_ALL;

        if (extras.getString("defaults") != null) {
            try {
                defaults = Integer.parseInt(extras.getString("defaults"));
            } catch (NumberFormatException e) {
            }
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(defaults)
                        .setSmallIcon(context.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(extras.getString("title"))
                        .setTicker(extras.getString("title"))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        String message = extras.getString("message");
        if (message != null) {
            mBuilder.setContentText(message);
        } else {
            mBuilder.setContentText("<missing message content>");
        }

        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }

        int notId = NOTIFICATION_ID;

        try {
            notId = Integer.parseInt(extras.getString("notId"));
        } catch (NumberFormatException e) {
            Log.e(LOGTAG,
                    "Number format exception - Error parsing Notification ID: " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOGTAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
        }

        //mNotificationManager.notify((String) appName, notId, mBuilder.build());
        mNotificationManager.notify(TAG, notId, mBuilder.build());

    }

    public void createNotificationEx(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        //mNotificationManager.cancel((String) getAppName(context), NOTIFICATION_ID);
        mNotificationManager.cancel(TAG, NOTIFICATION_ID);
    }

    private static String getAppName(Context context) {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(LOGTAG, "onError - errorId: " + errorId);
    }

}
