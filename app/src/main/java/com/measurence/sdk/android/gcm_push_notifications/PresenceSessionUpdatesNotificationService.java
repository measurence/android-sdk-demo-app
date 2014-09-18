/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Measurence Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.measurence.sdk.android.gcm_push_notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.demo.MainActivity;
import com.measurence.sdk.android.demo.R;

public class PresenceSessionUpdatesNotificationService extends IntentService {

    private final String LOG_TAG = "Measurence "+PresenceSessionUpdatesNotificationService.class.getSimpleName();

    public static final String SESSION_UPDATE_INTENT_ID = "SESSION_UPDATE";
    public static final String SESSION_UPDATE_JSON_PARAMETER = "SESSION_JSON";

    private LocalBroadcastManager localBroadcastManager;

    public PresenceSessionUpdatesNotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }


    private void sendNotification(String user, String store, String status) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.exclamation)
                        .setContentTitle(user)
                        .setContentText(store + ": " + status);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void notifySessionUpdateToUI(String presenceSessionUpdateJson) {
        Intent sessionUpdateNotificationIntent = new Intent(SESSION_UPDATE_INTENT_ID);
        sessionUpdateNotificationIntent.putExtra(SESSION_UPDATE_JSON_PARAMETER, presenceSessionUpdateJson);
        localBroadcastManager.sendBroadcast(sessionUpdateNotificationIntent);
        PresenceSessionUpdate update = PresenceSessionUpdate.fromJson(presenceSessionUpdateJson);
        sendNotification(update.getUserIdentities().get(0).getId(), update.getStoreKey(), update.getStatus());
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        v.vibrate(2000);

        long pattern[]={0,400,200,200,800};
        // 2nd argument is for repetition pass -1 if you do not want to repeat the Vibrate
        v.vibrate(pattern,-1);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            // The getMessageType() intent parameter must be the intent you received
            // in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    Log.i(LOG_TAG, "Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    Log.i(LOG_TAG, "Deleted messages on server: " + extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    String presenceSessionUpdateJson = extras.getString("user-session");
                    Log.i(LOG_TAG, "received|json|" + presenceSessionUpdateJson);
                    notifySessionUpdateToUI(presenceSessionUpdateJson);

                }
            }
        } finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // The device is no longer prevented from sleeping
            GcmPushNotificationsReceiver.completeWakefulIntent(intent);
        }
    }

}