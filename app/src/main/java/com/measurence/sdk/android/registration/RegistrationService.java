package com.measurence.sdk.android.registration;

import android.app.IntentService;
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
import com.measurence.sdk.android.util.DeviceMacAddress;

import java.io.IOException;

/**
 * This service register the device with the server. Does not check first if there is a registration id
 * already saved in the preferences, so that registration can be forced.
 */
public class RegistrationService extends IntentService {

    MeasurenceAPISubscriptions measurenceAPISubscriptions = new MeasurenceAPISubscriptions();
    private String LOG_TAG = "Measurence " + RegistrationService.class.getSimpleName();

    public RegistrationService() {
        super("RegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Registering device");
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            String registrationId = gcm.register(getString(R.string.SENDER_ID));
            Log.v(LOG_TAG, "Device registered, registration ID=" + registrationId);
            // Persist the regID - no need to register again.
            storeRegistrationId(getApplicationContext(), registrationId);

            String user_id = intent.getStringExtra("user_id");
            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            applyToApiSubscription(user_id);
            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.

        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error registering device", ex);
        }
    }
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = RegistrationUtil.getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prop_reg_id), regId);
        editor.putInt(getString(R.string.app_version), appVersion);
        editor.apply();
    }


    private void applyToServerSubscription(String user_identity) {
        // TODO. Possiamo anche avere un bottone per usare questo endpoint
    }

    private void applyToApiSubscription(String user_identity) {
        // Only for debugging purpouses

        Boolean applyToSubscriptionEnabled = true;
        Log.i(LOG_TAG, "apply to api subscription|enabled|" + applyToSubscriptionEnabled);
        if (!applyToSubscriptionEnabled) return;

        String partnerId = getString(R.string.partner_id);
        String registrationId = RegistrationUtil.getRegistrationId(getApplicationContext());

        AndroidPushSubscription androidPushSubscription = new AndroidPushSubscription(
                partnerId,
                user_identity,
                registrationId,
                DeviceMacAddress.get(this)
        );
        try {
            MeasurenceAPISubscriptions.SubscriptionResult subscriptionResult = measurenceAPISubscriptions.applyToAndroidPushNotification(androidPushSubscription);
            Log.i(LOG_TAG, "api subscription result|" + subscriptionResult);
        } catch (AndroidPushSubscriptionException e) {
            e.printStackTrace();
        }
    }
}

