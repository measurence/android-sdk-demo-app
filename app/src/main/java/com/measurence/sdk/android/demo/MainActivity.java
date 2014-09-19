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

package com.measurence.sdk.android.demo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.UserIdentity;
import com.measurence.sdk.android.api_subscription.MeasurenceApiSubscriptionService;
import com.measurence.sdk.android.api_subscription.gcm.AndroidApiSubscriptionService;
import com.measurence.sdk.android.api_subscription.gcm.GCMRegistrationIdUtil;
import com.measurence.sdk.android.api_subscription.httppost.HttpPostApiSubscriptionService;
import com.measurence.sdk.android.gcm_push_notifications.PresenceSessionUpdatesNotificationService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = "Measurence "+MainActivity.class.getSimpleName();
    static final class PresenceNotification  {
        PresenceSessionUpdate presenceSessionUpdate;
        Date received;
    }
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    ArrayAdapter<PresenceNotification> mAdapter;


    private BroadcastReceiver subscriptionResultBroadcastReceiver;

    private void displaySubscriptionResultMessage(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new NotificationsFragment())
                    .commit();
        }
        checkRegistration();
        subscriptionResultBroadcastReceiver = new BroadcastReceiver() {



            @Override
            public void onReceive(Context context, Intent intent) {
                String subscriptionResultMessage = intent.getStringExtra(MeasurenceApiSubscriptionService.SUBSCRIPTION_RESULT_INTENT_MESSAGE);
                displaySubscriptionResultMessage(subscriptionResultMessage);
            }
        };

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<PresenceNotification>(this,
                    R.layout.list_item_notification, R.id.list_item_notification_view, new ArrayList<PresenceNotification>()) {

                DateFormat dateFormat = DateFormat.getDateTimeInstance();

                private void setText(View root, int id, String text) {
                    TextView textView = (TextView) root.findViewById(id);
                    textView.setText(text);
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View itemView = inflater.inflate(R.layout.list_item_notification, null);
                    PresenceNotification notification = getItem(position);
                    PresenceSessionUpdate item =notification.presenceSessionUpdate;
                    String now = dateFormat.format(notification.received);
                    setText(itemView, R.id.list_item_notification_date, now);
                    StringBuilder sb = new StringBuilder();
                    for (UserIdentity userid : item.getUserIdentities()) {
                        sb.append(userid.getId()).append(" ");
                    }
                    setText(itemView, R.id.list_item_notification_userid, sb.toString());
                    setText(itemView, R.id.list_item_notification_storeid, item.getStoreKey());
                    setText(itemView, R.id.list_item_notification_new_user, getString(item.getIsNewVisitorInStore().booleanValue() ? R.string.yes : R.string.no));
                    setText(itemView, R.id.list_item_notification_status, item.getStatus());
                    setText(itemView, R.id.list_item_notification_session_start, dateFormat.format(item.getInterval().getStart()));
                    setText(itemView, R.id.list_item_notification_session_end, dateFormat.format(item.getInterval().getEnd()));
                    return itemView;
                }
            };
        }
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            private void displaySessionUpdate(PresenceSessionUpdate presenceSessionUpdate) {
                PresenceNotification notification = new PresenceNotification();
                notification.presenceSessionUpdate = presenceSessionUpdate;
                notification.received = new Date();
                mAdapter.insert(notification, 0);
                Log.i(LOG_TAG, "displaying session update|" + presenceSessionUpdate);
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                String presenceSessionUpdateJson = intent.getStringExtra(PresenceSessionUpdatesNotificationService.SESSION_UPDATE_JSON_PARAMETER);

                displaySessionUpdate(PresenceSessionUpdate.fromJson(presenceSessionUpdateJson));

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter(PresenceSessionUpdatesNotificationService.SESSION_UPDATE_INTENT_ID));

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((subscriptionResultBroadcastReceiver), new IntentFilter(MeasurenceApiSubscriptionService.SUBSCRIPTION_RESULT_INTENT_ID));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, R.string.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
        }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    private String getIdentityOfAppUser() {
        // Put here the logic which return the identity of the user of your APP (e.g. an email)
        return getString(R.string.user_identity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.force_subscription_to_android_apis) {
            Log.v(LOG_TAG, "force_subscription_to_android_apis");
            Intent registrationIntent = new Intent(this, AndroidApiSubscriptionService.class);
            registrationIntent.putExtra(MeasurenceApiSubscriptionService.REGISTRATION_INTENT_USER_IDENTITY, getIdentityOfAppUser());
            startService(registrationIntent);
        }

        if (id == R.id.force_subscription_to_http_apis) {
            Log.v(LOG_TAG, "force_subscription_to_http_apis");
            Intent registrationIntent = new Intent(this, HttpPostApiSubscriptionService.class);
            registrationIntent.putExtra(MeasurenceApiSubscriptionService.REGISTRATION_INTENT_USER_IDENTITY, getIdentityOfAppUser());
            startService(registrationIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkRegistration() {
        boolean playAvailable = checkPlayServices();
        if (!playAvailable) {
            return;
        }
        boolean isRegistered = GCMRegistrationIdUtil.checkRegistration(this);
        if (!isRegistered) {

            Intent registrationIntent = new Intent(this, MeasurenceApiSubscriptionService.class);
            // TODO
            registrationIntent.putExtra("user_id", "random_value");
            startService(registrationIntent);
        }
    }


}
