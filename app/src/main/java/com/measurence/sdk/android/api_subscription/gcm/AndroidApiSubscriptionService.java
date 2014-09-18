package com.measurence.sdk.android.api_subscription.gcm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.measurence.sdk.android.api_subscriptions.AndroidPushSubscription;
import com.measurence.sdk.android.api_subscriptions.AndroidPushSubscriptionException;
import com.measurence.sdk.android.api_subscriptions.MeasurenceAPISubscriptions;
import com.measurence.sdk.android.demo.R;
import com.measurence.sdk.android.api_subscription.MeasurenceApiSubscriptionService;
import com.measurence.sdk.android.util.DeviceMacAddress;

import java.io.IOException;

public class AndroidApiSubscriptionService extends MeasurenceApiSubscriptionService {

    private String LOG_TAG = "Measurence " + MeasurenceApiSubscriptionService.class.getSimpleName();

    private void obtainAndStoreRegistrationId(String gcmProjectNumber) throws IOException {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        String gcmRegistrationId = gcm.register(gcmProjectNumber);
        storeRegistrationId(getApplicationContext(), gcmRegistrationId);
        Log.v(LOG_TAG, "GCM Project number|" + gcmProjectNumber + "|registration ID|" + gcmRegistrationId);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = GCMRegistrationIdUtil.getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prop_reg_id), regId);
        editor.putInt(getString(R.string.app_version), appVersion);
        editor.apply();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String gcmProjectNumber = getString(R.string.GOOGLE_CLOUD_MESSAGING_PROJECT_NUMBER);
        try {
            obtainAndStoreRegistrationId(gcmProjectNumber);
        } catch (IOException e) {
            Log.e(LOG_TAG, "cannot get a registration id|", e);
        }

        super.onHandleIntent(intent);
    }

    @Override
    protected void applyToSubscriptionAndNotifyResult(String user_identity) {
        try {
            String registrationId = GCMRegistrationIdUtil.getRegistrationId(getApplicationContext());
            AndroidPushSubscription androidPushSubscription = new AndroidPushSubscription(
                    getMeasurencePartnerId(),
                    user_identity,
                    registrationId,
                    DeviceMacAddress.get(this)
            );
            MeasurenceAPISubscriptions.SubscriptionResult subscriptionResult = measurenceAPISubscriptions.applyToAndroidPushNotification(androidPushSubscription);

            Log.i(LOG_TAG, "api subscription result|" + subscriptionResult);
            notifySubscriptionResult("Subscription succeeded. Outcome: \"" + subscriptionResult + "\"");
        } catch (AndroidPushSubscriptionException e) {
            notifySubscriptionResult("Subscription failed. Error: \"" + e.getMessage() + "\"");
            e.printStackTrace();
        }
    }

}
