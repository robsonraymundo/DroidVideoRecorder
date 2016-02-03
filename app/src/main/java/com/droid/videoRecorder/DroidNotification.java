package com.droid.videoRecorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Robson on 03/02/2016.
 */

public class DroidNotification extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // super.onNotificationPosted(sbn);
        String msgNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            msgNotification = getNotificationKitKat(sbn);
        } else {
            msgNotification = getNotification(sbn.getNotification());
        }

        if (!msgNotification.isEmpty()) {
            if (msgNotification.contains("DVR=")) {
                if (msgNotification != "") {
                    Intent mIntent = new Intent();
                    mIntent.setAction("DVRREC");
                    mIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    mIntent.putExtra("DVRREC", msgNotification);
                    sendBroadcast(mIntent);
                }
            }

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private String getNotificationKitKat(StatusBarNotification mStatusBarNotification) {
        String pack = mStatusBarNotification.getPackageName();// Package Name
        Bundle extras = mStatusBarNotification.getNotification().extras;
        CharSequence tit = extras.getCharSequence(Notification.EXTRA_TITLE); // Title
        CharSequence desc = extras.getCharSequence(Notification.EXTRA_TEXT); // / Description
        String msg = "";
        if (desc == null) {
            Bundle bigExtras = mStatusBarNotification.getNotification().extras;
            CharSequence[] descArray = bigExtras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            msg = descArray[descArray.length-1].toString();
        }
        else msg = desc.toString();

        if (msg.contains("DVR=")) {
            return msg;
        } else return "";
    }

    public static Object getObjectProperty(Object object, String propertyName) {
        try {
            Field f = object.getClass().getDeclaredField(propertyName);
            f.setAccessible(true);
            return f.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getNotification(Notification notif) {
        String sValue;
        String sMethodName;
        try {

            ArrayList<Object> actions = (ArrayList<Object>) getObjectProperty(getObjectProperty(notif, "bigContentView"), "mActions");
            Field fMethodName;
            Field fValue;
            for (int i = actions.size() - 1; i >= actions.size() - 2; i--) {
                fMethodName = actions.get(i).getClass().getDeclaredField("methodName");
                fMethodName.setAccessible(true);
                sMethodName = fMethodName.get(actions.get(i)).toString();
                try {
                    if (sMethodName.equals("setText")) {
                        fValue = actions.get(i).getClass().getDeclaredField("value");
                        fValue.setAccessible(true);
                        sValue = fValue.get(actions.get(i)).toString();
                        if (sValue.contains("DVR=")) {
                            return sValue;
                        }
                        break;
                    }
                } catch (Exception ex) {
                    //sValue = ex.getMessage();
                }
            }
        } catch (Exception ex) {
            String mEx = ex.getMessage();
        }
        return "";

    }
}