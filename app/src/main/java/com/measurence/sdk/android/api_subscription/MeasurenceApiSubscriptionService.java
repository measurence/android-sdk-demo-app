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

package com.measurence.sdk.android.api_subscription;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.measurence.sdk.android.api_subscriptions.MeasurenceAPISubscriptions;
import com.measurence.sdk.android.demo.R;

public abstract class MeasurenceApiSubscriptionService extends IntentService {

    public static final String SUBSCRIPTION_RESULT_INTENT_ID = "SUBSCRIPTION_RESULT";
    public static final String SUBSCRIPTION_RESULT_INTENT_MESSAGE = "SUBSCRIPTION_RESULT_INTENT_MESSAGE";

    public static final String REGISTRATION_INTENT_USER_IDENTITY = "USER_IDENTITY";

    protected MeasurenceAPISubscriptions measurenceAPISubscriptions = new MeasurenceAPISubscriptions();

    private LocalBroadcastManager localBroadcastManager;

    public MeasurenceApiSubscriptionService() {
        super("RegistrationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    protected void notifySubscriptionResult(String subscriptionResultMessage) {
        Intent subscriptionResultIntent = new Intent(SUBSCRIPTION_RESULT_INTENT_ID);
        subscriptionResultIntent.putExtra(SUBSCRIPTION_RESULT_INTENT_MESSAGE, subscriptionResultMessage);
        localBroadcastManager.sendBroadcast(subscriptionResultIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String user_id = intent.getStringExtra(REGISTRATION_INTENT_USER_IDENTITY);
        applyToSubscriptionAndNotifyResult(user_id);
    }

    protected String getMeasurencePartnerId() {
        return getString(R.string.MEASURENCE_PARTNER_ID);
    }

    protected abstract void applyToSubscriptionAndNotifyResult(String user_identity);
}

