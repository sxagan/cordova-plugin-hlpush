/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package com.datum.hotline.plugin.hlpush.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.media.RingtoneManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

/**
 * Builder class for local notifications. Build fully configured local
 * notification specified by JSON object passed from JS side.
 */
public class Builder {

    // Application context passed by constructor
    private final Context context;

    // Notification options passed by JS
    private final Options options;

    // Receiver to handle the trigger event
    private Class<?> triggerReceiver;

    // Receiver to handle the clear event
    private Class<?> clearReceiver = ClearReceiver.class;

    // Activity to handle the click event
    private Class<?> clickActivity = ClickActivity.class;

    private static String TAG = "LN-Builder";

    /**
     * Constructor
     *
     * @param context
     *      Application context
     * @param options
     *      Notification options
     */
    public Builder(Context context, JSONObject options) {
        this.context = context;
        this.options = new Options(context).parse(options);
    }

    /**
     * Constructor
     *
     * @param options
     *      Notification options
     */
    public Builder(Options options) {
        this.context = options.getContext();
        this.options = options;
    }

    /**
     * Set trigger receiver.
     *
     * @param receiver
     *      Broadcast receiver
     */
    public Builder setTriggerReceiver(Class<?> receiver) {
        this.triggerReceiver = receiver;
        return this;
    }

    /**
     * Set clear receiver.
     *
     * @param receiver
     *      Broadcast receiver
     */
    public Builder setClearReceiver(Class<?> receiver) {
        this.clearReceiver = receiver;
        return this;
    }

    /**
     * Set click activity.
     *
     * @param activity
     *      Activity
     */
    public Builder setClickActivity(Class<?> activity) {
        this.clickActivity = activity;
        return this;
    }

    /**
     * Creates the notification with all its options passed through JS.
     */
    public Notification buildEx() {
        Uri sound = options.getSoundUri();
        NotificationCompat.BigTextStyle style;
        NotificationCompat.Builder builder;

        String summary = options.getText();
        if(summary.length() > 50){
            summary = summary.substring(0,20);
        }
        style = new NotificationCompat.BigTextStyle()
                .bigText(options.getText()).setBigContentTitle(options.getTitle());


        builder = new NotificationCompat.Builder(context)
                .setDefaults(0)
                .setContentTitle(options.getTitle())
                //.setContentText(options.getText())
                .setContentText(summary)
                .setNumber(options.getBadgeNumber())
                //.setTicker(options.getText())
                .setTicker(summary)
                .setSmallIcon(options.getSmallIcon())
                .setLargeIcon(options.getIconBitmap())
                //.setAutoCancel(options.isAutoClear())
                .setAutoCancel(true)
                .setOngoing(options.isOngoing())
                .setStyle(style)
                .setLights(options.getLedColor(), 500, 500);

        /*Intent view2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        PendingIntent piview2 = PendingIntent.getService(this.context, 0, view2, 0);
        Intent view3 = new Intent(this.context, ClickActivity.class);//com.datum.hotline.plugin.hlpush.localnotification.
        view3.putExtra("Value1", "This value one for ActivityTwo");
        view3.putExtra("Value2", "This value two ActivityTwo");
        PendingIntent piview3 = PendingIntent.getService(this.context, 0, view3, 0);

        builder.addAction(this.context.getResources().getIdentifier("icon", "drawable", this.context.getPackageName()), "Click", piview3)
                .addAction(this.context.getResources().getIdentifier("icon", "drawable", this.context.getPackageName()), "View", piview2);*/

        /*if (sound != null) {
            builder.setSound(sound);
        }*/
        String soundname = options.getSoundName();
        if (soundname != null) {
            String pkg = this.context.getPackageName();
            //int resId = this.context.getResources().getIdentifier(soundname, "raw", this.context.getPackageName());
            //Uri s = Uri.parse("android.resource://" + pkg + "/" + resId);

            //Uri s = Uri.parse("android.resource://"+pkg+"/raw/s1");
            Uri s = Uri.parse("android.resource://"+pkg+soundname);
            //Uri s = Uri.parse("res://" + R.raw.s1);
            //builder.setSound(sound);
            //this.context.getResources().getIdentifier("s1", "raw", this.context.getPackageName())
            builder.setSound(s);
            //builder.setSound(Uri.parse("android.resource://de.appplant.localnotification.example/s1"));
        }

        applyDeleteReceiver(builder);
        applyContentReceiver(builder);

        return new Notification(context, options, builder, triggerReceiver);
    }

    public Notification build() {
        Uri sound = options.getSoundUri();
        //String soundpath = options.getSoundPath();
        NotificationCompat.BigTextStyle style;
        NotificationCompat.Builder builder;

        //Log.d(TAG,"Sound file path: "+soundpath);
        Log.d(TAG,"Sound file uri: "+sound.toString());
        boolean soundFound = true;
        if(sound.toString() != ""){
            File file = new File(sound.toString());
            if (!file.exists()){
                Log.d(TAG,"File not found: "+sound.toString());
                soundFound = false;
            }
        }

        /*String summary = options.getText();
        if(summary.length() > 50){
            summary = summary.substring(0,20);
        }*/
        JSONObject optionsjson = options.getDict();
        JSONArray dataArray = optionsjson.optJSONArray("dataArray");

        String BTS_BigText = "BigTextStyle-BigText",BTS_BigTitle ="BigTextStyle-BigTitle",BTS_SummaryText="SummaryText";
        String ContentTitle="ContentTitle",ContentText="ContentText",Ticker="Ticker";
        int SmallIcon,LedColor;
        Bitmap IconBitmap = options.getIconBitmap();
        boolean isAutoClear = true;
        int badge = 0;
        LedColor = options.getLedColor();
        SmallIcon = options.getSmallIcon();
        IconBitmap = options.getIconBitmap();
        isAutoClear = options.isAutoClear();
        if(dataArray != null){
            badge = dataArray.length();
        }
        if(badge > 1){
            try {
                JSONObject dat = new JSONObject(optionsjson.optString("data"));
                BTS_BigTitle = "HotLines";
                String msg = "";
                for(int i = 0 ; i < dataArray.length(); i++){
                    JSONObject obj = new JSONObject(dataArray.getString(i));
                    if(msg.length() > 0){
                        msg = msg + "\r\n";
                    }
                    String addMSg = obj.optString("msg");
                    if(addMSg.length() > 32){
                        addMSg = addMSg.substring(0,30) + " ...";
                    }
                    msg = msg + obj.optString("sender") + ": "+ addMSg;
                }
                BTS_BigText = msg;
                BTS_SummaryText = String.format("%1d new messages", badge);
                ContentTitle = BTS_BigTitle;
                ContentText = BTS_SummaryText;
                Ticker = BTS_SummaryText;

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("lNtfy", "Error parsing JSONObject 1");
            }

        }else{
            try {
                JSONObject dat = new JSONObject(optionsjson.optString("data"));
                BTS_BigTitle = dat.getString("sender");
                String msg = dat.getString("msg");
                /*if(msg.length() > 45){
                    msg = msg.substring(0,42) + " ...";
                }*/
                BTS_BigText = msg;
                //BTS_SummaryText = String.format("New message in %1s post", dat.getString("posttitle"));
                BTS_SummaryText = "";
                try{
                    if(dat.has("json")){
                        JSONObject json = dat.getJSONObject("json");
                        String groupName = json.getString("groupname");
                        if(groupName != "" && groupName != null){
                            BTS_SummaryText = groupName;
                        }
                    }

                }
                catch(JSONException e){
                    e.printStackTrace();
                    Log.e("lNtfy", "Error parsing getting JSONObject 'json'");
                }
                ContentTitle = BTS_BigTitle;
                ContentText = BTS_BigText;
                Ticker = BTS_SummaryText;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("lNtfy", "Error parsing JSONObject 2");
            }

        }

        style = new NotificationCompat.BigTextStyle()
                //.bigText(options.getText()).setBigContentTitle(options.getTitle());
                //.bigText(BTS_BigText).setBigContentTitle(BTS_BigTitle).setSummaryText(BTS_SummaryText);
                .bigText(BTS_BigText).setBigContentTitle(BTS_BigTitle);
        if (BTS_SummaryText != null && BTS_SummaryText != ""){
            style.setSummaryText(BTS_SummaryText);
        }


        builder = new NotificationCompat.Builder(context)
                .setDefaults(0)
                //.setContentTitle(options.getTitle())
                .setContentTitle(ContentTitle)
                .setContentText(ContentText)
                        //.setContentText(options.getText())
                //.setContentText(summary)
                //.setNumber(options.getBadgeNumber())
                .setNumber(badge)
                .setColor(Color.YELLOW)
                        //.setTicker(options.getText())
                //.setTicker(summary)
                .setTicker(Ticker)
                .setSmallIcon(SmallIcon)
                .setLargeIcon(IconBitmap)
                .setAutoCancel(isAutoClear)
                .setOngoing(false)
                .setStyle(style)
                .setLights(LedColor, 500, 500);
        /*if(soundFound){
            Log.d(TAG,"Build sound using file path: "+sound.toString());
            builder.setSound(sound);
        }else{
            Uri sUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Log.d(TAG,"Build sound default ringtone: "+sUri.toString());
            builder.setSound(sUri);
        }*/
        builder.setSound(sound);

        /*Intent view2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        PendingIntent piview2 = PendingIntent.getService(this.context, 0, view2, 0);
        Intent view3 = new Intent(this.context, ClickActivity.class);//de.appplant.cordova.plugin.localnotification.
        view3.putExtra("Value1", "This value one for ActivityTwo");
        view3.putExtra("Value2", "This value two ActivityTwo");
        PendingIntent piview3 = PendingIntent.getService(this.context, 0, view3, 0);

        builder.addAction(this.context.getResources().getIdentifier("icon", "drawable", this.context.getPackageName()), "Click", piview3)
                .addAction(this.context.getResources().getIdentifier("icon", "drawable", this.context.getPackageName()), "View", piview2);*/

        /*if (sound != null) {
            builder.setSound(sound);
        }*/
        /*String soundname = options.getSoundName();
        if (soundname != null) {
            String pkg = this.context.getPackageName();
            //int resId = this.context.getResources().getIdentifier(soundname, "raw", this.context.getPackageName());
            //Uri s = Uri.parse("android.resource://" + pkg + "/" + resId);

            //Uri s = Uri.parse("android.resource://"+pkg+"/raw/s1");
            Uri s = Uri.parse("android.resource://"+pkg+soundname);
            //Uri s = Uri.parse("res://" + R.raw.s1);
            //builder.setSound(sound);
            //this.context.getResources().getIdentifier("s1", "raw", this.context.getPackageName())
            builder.setSound(s);
            //builder.setSound(Uri.parse("android.resource://de.appplant.localnotification.example/s1"));
        }*/

        applyDeleteReceiver(builder);
        applyContentReceiver(builder);

        return new Notification(context, options, builder, triggerReceiver);
    }

    /**
     * Set intent to handle the delete event. Will clean up some persisted
     * preferences.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyDeleteReceiver(NotificationCompat.Builder builder) {

        if (clearReceiver == null)
            return;

        Intent deleteIntent = new Intent(context, clearReceiver)
                .setAction(options.getIdStr())
                .putExtra(Options.EXTRA, options.toString());

        PendingIntent dpi = PendingIntent.getBroadcast(
                context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setDeleteIntent(dpi);
    }

    /**
     * Set intent to handle the click event. Will bring the app to
     * foreground.
     *
     * @param builder
     *      Local notification builder instance
     */
    private void applyContentReceiver(NotificationCompat.Builder builder) {

        if (clickActivity == null)
            return;

        Intent intent = new Intent(context, clickActivity)
                .putExtra(Options.EXTRA, options.toString())
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        int requestCode = new Random().nextInt();

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(contentIntent);
    }

}
