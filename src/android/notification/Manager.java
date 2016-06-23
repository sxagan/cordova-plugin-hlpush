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

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.datum.hotline.plugin.hlpush.notification.Notification.PREF_KEY;

/**
 * Central way to access all or single local notifications set by specific
 * state like triggered or scheduled. Offers shortcut ways to schedule,
 * cancel or clear local notifications.
 */
public class Manager {

    // Context passed through constructor and used for notification builder.
    private Context context;

    /**
     * Constructor
     *
     * @param context
     *      Application context
     */
    private Manager(Context context){
        this.context = context;
    }

    /**
     * Static method to retrieve class instance.
     *
     * @param context
     *      Application context
     */
    public static Manager getInstance(Context context) {
        return new Manager(context);
    }

    /**
     * Schedule local notification specified by JSON object.
     *
     * @param options
     *      JSON object with set of options
     * @param receiver
     *      Receiver to handle the trigger event
     */
    public Notification schedule (JSONObject options, Class<?> receiver) {
        return schedule(new Options(context).parse(options), receiver);
    }

    /**
     * Schedule local notification specified by options object.
     *
     * @param options
     *      Set of notification options
     * @param receiver
     *      Receiver to handle the trigger event
     */
    public Notification schedule (Options options, Class<?> receiver) {
        Notification notification = new Builder(options)
                .setTriggerReceiver(receiver)
                .build();

        notification.schedule();

        return notification;
    }

    /**
     * Clear local notification specified by ID.
     *
     * @param id
     *      The notification ID
     * @param updates
     *      JSON object with notification options
     * @param receiver
     *      Receiver to handle the trigger event
     */
    public Notification update (int id, JSONObject updates, Class<?> receiver) {
        Notification notification = get(id);

        if (notification == null)
            return null;

        notification.cancel();
        
        JSONObject options = mergeJSONObjects( notification.getOptions().getDict(), updates);

        try {
            options.putOpt("updatedAt", new Date().getTime());
        } catch (JSONException ignore) {}

        return schedule(options, receiver);
    }

    /*public Notification scheduleEx (JSONObject options, Class<?> receiver) {
        return schedule(new Options(context).parse(options), receiver);
    }
    public Notification scheduleEx (Options options, Class<?> receiver) {
        Notification notification = new Builder(options)
                .setTriggerReceiver(receiver)
                .buildEx();

        notification.schedule();

        return notification;
    }*/
    /**
     * Clear local notification specified by ID.
     *
     * @param id
     *      The notification ID
     * @param appends
     *      JSON object with notification options
     * @param receiver
     *      Receiver to handle the trigger event
     */
    public Notification append (int id, JSONObject appends,Context ctx, Class<?> receiver) {
        Notification notification = get(id);

        String pkgName = ctx.getPackageName();
        SharedPreferences sharedPref = ctx.getSharedPreferences(pkgName,ctx.MODE_PRIVATE);


        if (notification == null){
            /*SharedPreferences.Editor editor1 = sharedPref.edit();
            editor1.putString("push", "");
            Boolean as = editor1.commit();*/

            JSONArray arr = new JSONArray();
            String s = sharedPref.getString("push", "");
            if(s.length() > 0){
                try {
                    arr = new JSONArray(s);
                } catch (JSONException e) { e.printStackTrace(); }
            }

            JSONObject adata = null;
            try {
                adata = new JSONObject((String) appends.opt("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String msg = (String) adata.opt("msg");
            if(msg.length() == 0){
                arr.put(adata);
                String arrStr = arr.toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("push", arrStr);
                Boolean a = editor.commit();
                return null;
            }else{
                JSONArray dataArray = new JSONArray();
                if(arr.length() >0){
                    for (int i = 0; i < arr.length(); i++){
                        JSONObject rs = null;
                        try {
                            rs = (JSONObject) arr.get(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dataArray.put(rs.toString());
                    }
                    try {
                        appends.putOpt("dataArray", dataArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("push", "");
                    Boolean a = editor.commit();
                }
                return schedule(appends, receiver);
            }
        }else{
            /*String s = sharedPref.getString("push", "bbb");
            Log.d("lNtfy-append", "sharepref data: "+ s);*/
            JSONArray dataArray = new JSONArray();
            String s = sharedPref.getString("push", "");
            if(s.length() > 0){
                try {
                    JSONArray tarr = new JSONArray(s);
                    for (int i = 0; i < tarr.length(); i++){
                        JSONObject rs = (JSONObject) tarr.get(i);
                        dataArray.put(rs.toString());
                    }
                    //dataArray = concatArray(dataArray,tarr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("push", "");
            Boolean a = editor.commit();

            JSONObject oldOptObj = notification.getOptions().getDict();
            Options oldOptions = new Options(context).parse(oldOptObj);
            String oldtext = oldOptions.getText();

            Log.d("lNtfy-append", "insert to dataArray");
            if (oldOptObj.has("dataArray")) {
                //dataArray = oldOptObj.optJSONArray("dataArray");
                try {
                    dataArray = concatArray(dataArray,oldOptObj.optJSONArray("dataArray"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                //dataArray = new JSONArray();
                dataArray.put(oldOptObj.opt("data"));
            }

            dataArray.put(appends.opt("data"));


            try {
                oldOptObj.putOpt("dataArray", dataArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            String newtext = appends.optString("text");
            try {
                appends.put("text", oldtext+"\r\n" + newtext);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject options = mergeJSONObjects( oldOptObj, appends);

            try {
                options.putOpt("updatedAt", new Date().getTime());
            } catch (JSONException ignore) {}

            notification.cancel();

            return schedule(options, receiver);

        }


    }

    private JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    /**
     * Clear local notification specified by ID.
     *
     * @param id
     *      The notification ID
     */
    public Notification clear (int id) {
        Notification notification = get(id);

        if (notification != null) {
            notification.clear();
        }

        return notification;
    }

    /**
     * Clear local notification specified by ID.
     *
     * @param id
     *      The notification ID
     */
    public Notification cancel (int id) {
        Notification notification = get(id);

        if (notification != null) {
            notification.cancel();
        }

        return notification;
    }

    /**
     * Clear all local notifications.
     */
    public void clearAll () {
        List<Notification> notifications = getAll();

        for (Notification notification : notifications) {
            notification.clear();
        }

        getNotMgr().cancelAll();
    }

    /**
     * Cancel all local notifications.
     */
    public void cancelAll () {
        List<Notification> notifications = getAll();

        for (Notification notification : notifications) {
            notification.cancel();
        }

        getNotMgr().cancelAll();
    }

    /**
     * All local notifications IDs.
     */
    public List<Integer> getIds() {
        Set<String> keys = getPrefs().getAll().keySet();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (String key : keys) {
            ids.add(Integer.parseInt(key));
        }

        return ids;
    }

    /**
     * All local notification IDs for given type.
     *
     * @param type
     *      The notification life cycle type
     */
    public List<Integer> getIdsByType(Notification.Type type) {
        List<Notification> notifications = getAll();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (Notification notification : notifications) {
            if (notification.getType() == type) {
                ids.add(notification.getId());
            }
        }

        return ids;
    }

    /**
     * List of local notifications with matching ID.
     *
     * @param ids
     *      Set of notification IDs
     */
    public List<Notification> getByIds(List<Integer> ids) {
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        for (int id : ids) {
            Notification notification = get(id);

            if (notification != null) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * List of all local notification.
     */
    public List<Notification> getAll() {
        return getByIds(getIds());
    }

    /**
     * List of local notifications from given type.
     *
     * @param type
     *      The notification life cycle type
     */
    public List<Notification> getByType(Notification.Type type) {
        List<Notification> notifications = getAll();
        ArrayList<Notification> list = new ArrayList<Notification>();

        if (type == Notification.Type.ALL)
            return notifications;

        for (Notification notification : notifications) {
            if (notification.getType() == type) {
                list.add(notification);
            }
        }

        return list;
    }

    /**
     * List of local notifications with matching ID from given type.
     *
     * @param type
     *      The notification life cycle type
     * @param ids
     *      Set of notification IDs
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<Notification> getBy(Notification.Type type, List<Integer> ids) {
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        for (int id : ids) {
            Notification notification = get(id);

            if (notification != null && notification.isScheduled()) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * If a notification with an ID exists.
     *
     * @param id
     *      Notification ID
     */
    public boolean exist (int id) {
        return get(id) != null;
    }

    /**
     * If a notification with an ID and type exists.
     *
     * @param id
     *      Notification ID
     * @param type
     *      Notification type
     */
    public boolean exist (int id, Notification.Type type) {
        Notification notification = get(id);

        return notification != null && notification.getType() == type;
    }

    /**
     * List of properties from all local notifications.
     */
    public List<JSONObject> getOptions() {
        return getOptionsById(getIds());
    }

    /**
     * List of properties from local notifications with matching ID.
     *
     * @param ids
     *      Set of notification IDs
     */
    public List<JSONObject> getOptionsById(List<Integer> ids) {
        ArrayList<JSONObject> options = new ArrayList<JSONObject>();

        for (int id : ids) {
            Notification notification = get(id);

            if (notification != null) {
                options.add(notification.getOptions().getDict());
            }
        }

        return options;
    }

    /**
     * List of properties from all local notifications from given type.
     *
     * @param type
     *      The notification life cycle type
     */
    public List<JSONObject> getOptionsByType(Notification.Type type) {
        ArrayList<JSONObject> options = new ArrayList<JSONObject>();
        List<Notification> notifications = getByType(type);

        for (Notification notification : notifications) {
            options.add(notification.getOptions().getDict());
        }

        return options;
    }

    /**
     * List of properties from local notifications with matching ID from
     * given type.
     *
     * @param type
     *      The notification life cycle type
     * @param ids
     *      Set of notification IDs
     */
    public List<JSONObject> getOptionsBy(Notification.Type type,
                                         List<Integer> ids) {

        if (type == Notification.Type.ALL)
            return getOptionsById(ids);

        ArrayList<JSONObject> options = new ArrayList<JSONObject>();
        List<Notification> notifications = getByIds(ids);

        for (Notification notification : notifications) {
            if (notification.getType() == type) {
                options.add(notification.getOptions().getDict());
            }
        }

        return options;
    }

    /**
     * Get existent local notification.
     *
     * @param id
     *      Notification ID
     */
    public Notification get(int id) {
        Map<String, ?> alarms = getPrefs().getAll();
        String notId          = Integer.toString(id);
        JSONObject options;

        if (!alarms.containsKey(notId))
            return null;


        try {
            String json = alarms.get(notId).toString();
            options = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        Builder builder = new Builder(context, options);

        return builder.build();
    }

    /**
     * Merge two JSON objects.
     *
     * @param obj1
     *      JSON object
     * @param obj2
     *      JSON object with new options
     */
    private JSONObject mergeJSONObjects (JSONObject obj1, JSONObject obj2) {
        Iterator it = obj2.keys();

        while (it.hasNext()) {
            try {
                String key = (String)it.next();

                obj1.put(key, obj2.opt(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return obj1;
    }

    /**
     * Shared private preferences for the application.
     */
    private SharedPreferences getPrefs () {
        return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Notification manager for the application.
     */
    private NotificationManager getNotMgr () {
        return (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
