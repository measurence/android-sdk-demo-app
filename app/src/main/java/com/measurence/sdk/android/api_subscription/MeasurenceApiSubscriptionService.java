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

