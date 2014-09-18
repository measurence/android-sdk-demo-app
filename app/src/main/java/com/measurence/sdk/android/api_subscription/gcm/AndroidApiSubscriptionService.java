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
