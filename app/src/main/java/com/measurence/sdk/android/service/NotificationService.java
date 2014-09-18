package com.measurence.sdk.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.demo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends IntentService {

    private final String LOG_TAG = "Measurence "+NotificationService.class.getSimpleName();

    public NotificationService() {
        super("NotificationService");
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

                    PresenceSessionUpdate presenceSessionUpdate = PresenceSessionUpdate.fromJson(presenceSessionUpdateJson);
                    Log.i(LOG_TAG, "received|presence event" + presenceSessionUpdate);
                }
            }
        } finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // The device is no longer prevented from sleeping
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

}