package com.plugin.gcm;

import com.google.android.gcm.GCMBaseIntentService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import com.datum.hotline.plugin.hlpush.localnotification.TriggerReceiver;
import com.datum.hotline.plugin.hlpush.notification.Manager;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

    //public static final int NOTIFICATION_ID = 237;
    public static final int NOTIFICATION_ID = 1;

    private static String LOGTAG = "PushPlugin-GCMIntentService";
    static final String TAG = "HotlineNotifications";

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
                Bundle payload = extras.getBundle("payload");
                Bundle data = extras.getBundle("data");

                //if (extras.getString(MESSAGE) != null && extras.getString(MESSAGE).length() != 0){
                if (data.getString(MESSAGE) != null && data.getString(MESSAGE).length() != 0){
                    JSONObject appends = ToJson(extras);
                    try {
                        appends.put("id",1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Manager.getInstance(context).append(1, appends,TriggerReceiver.class);
                }
            }

            NotificationService.getInstance(context).onMessage(extras);
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
