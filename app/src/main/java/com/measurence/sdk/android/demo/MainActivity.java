package com.measurence.sdk.android.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.registration.RegistrationService;
import com.measurence.sdk.android.registration.RegistrationUtil;
import com.measurence.sdk.android.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final String LOG_TAG = "Measurence "+MainActivity.class.getSimpleName();

    private BroadcastReceiver broadcastReceiver;

    private List<String> sessionUpdatesList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        checkRegistration();

        broadcastReceiver = new BroadcastReceiver() {

            private void displaySessionUpdate(PresenceSessionUpdate presenceSessionUpdate) {
                ListView sessionUpdatesListView = (ListView) findViewById(R.id.sessionUpdatesListView);
                sessionUpdatesList.add(0, presenceSessionUpdate.toString());
                Log.i(LOG_TAG, "displaying session update|" + presenceSessionUpdate);
//                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getBaseContext(), R.layout., sessionUpdatesList);
//                sessionUpdatesListView.setAdapter(adapter1);
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                String presenceSessionUpdateJson = intent.getStringExtra(NotificationService.SESSION_UPDATE_JSON_PARAMETER);
                displaySessionUpdate(PresenceSessionUpdate.fromJson(presenceSessionUpdateJson));
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter(NotificationService.SESSION_UPDATE_INTENT_ID));
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO: create force registration menu and settings menu
            Log.v(LOG_TAG, "Force registration");
            Intent registrationIntent = new Intent(this, RegistrationService.class);
            registrationIntent.putExtra("user_id", "random_value");
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
        boolean isRegistered = RegistrationUtil.checkRegistration(this);
        if (!isRegistered) {

            Intent registrationIntent = new Intent(this, RegistrationService.class);
            // TODO
            registrationIntent.putExtra("user_id", "random_value");
            startService(registrationIntent);
        }
    }
}
