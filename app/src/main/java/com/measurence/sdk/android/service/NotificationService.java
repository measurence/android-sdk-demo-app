package com.measurence.sdk.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class NotificationService extends IntentService {

    private final String LOG_TAG = "Measurence "+NotificationService.class.getSimpleName();

    public static final String SESSION_UPDATE_INTENT_ID = "SESSION_UPDATE";
    public static final String SESSION_UPDATE_JSON_PARAMETER = "SESSION_JSON";

    private LocalBroadcastManager localBroadcastManager;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    private void notifySessionUpdateToUI(String presenceSessionUpdateJson) {
        Intent sessionUpdateNotificationIntent = new Intent(SESSION_UPDATE_INTENT_ID);
        sessionUpdateNotificationIntent.putExtra(SESSION_UPDATE_JSON_PARAMETER, presenceSessionUpdateJson);
        localBroadcastManager.sendBroadcast(sessionUpdateNotificationIntent);
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
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

}