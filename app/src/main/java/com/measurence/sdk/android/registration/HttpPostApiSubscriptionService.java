package com.measurence.sdk.android.registration;

import android.util.Log;

import com.measurence.sdk.android.api_subscriptions.HttpPostSubscription;
import com.measurence.sdk.android.api_subscriptions.HttpPostSubscriptionException;
import com.measurence.sdk.android.api_subscriptions.MeasurenceAPISubscriptions;
import com.measurence.sdk.android.util.DeviceMacAddress;

public class HttpPostApiSubscriptionService extends MeasurenceApiSubscriptionService {

    private String LOG_TAG = "Measurence " + MeasurenceApiSubscriptionService.class.getSimpleName();

    @Override
    protected void applyToSubscriptionAndNotifyResult(String user_identity) {
        try {
            HttpPostSubscription androidHttpPostSubscription = new HttpPostSubscription(
                    getMeasurencePartnerId(),
                    user_identity,
                    DeviceMacAddress.get(this)
            );
            MeasurenceAPISubscriptions.SubscriptionResult subscriptionResult = measurenceAPISubscriptions.applyToBackEndHttpPostNotification(androidHttpPostSubscription);

            Log.i(LOG_TAG, "http post subscription result|" + subscriptionResult);
            notifySubscriptionResult("HTTP Post Subscription succeeded. Outcome: \"" + subscriptionResult + "\"");
        } catch (HttpPostSubscriptionException e) {
            notifySubscriptionResult("HTTP Post Subscription failed. Error: \"" + e.getMessage() + "\"");
            e.printStackTrace();
        }
    }
}
