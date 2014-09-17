package com.measurence.sdk.android.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.measurence.sdk.android.demo.R;

/**
 * This Broadcast Receiver will delegate handling of the message to
 * the proper IntentService, while forbidding the device to sleep while
 * the intent is being handled
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        final String LOG_TAG = context.getString(R.string.log_prefix) + " "+GcmBroadcastReceiver.class.getSimpleName();
        Log.v(LOG_TAG, "Received intent");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        Log.v(LOG_TAG, "Intent processed");
    }
}
