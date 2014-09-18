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

            // We handle only MESSAGE_TYPE_MESSAGE intents
            String messageType = gcm.getMessageType(intent);
            if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
                // Unbundle message data
                String jsonString = extras.getString(getString(R.string.notification_payload_key));
                PresenceSessionUpdate presenceSessionUpdate = PresenceSessionUpdate.fromJson(jsonString);
                Log.i(LOG_TAG, "presence session update|" + presenceSessionUpdate);
            }
        } finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // The device is no longer prevented from sleeping
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

}