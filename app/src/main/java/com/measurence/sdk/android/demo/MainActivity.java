package com.measurence.sdk.android.demo;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.measurence.sdk.android.registration.RegistrationService;
import com.measurence.sdk.android.registration.RegistrationUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = "Measurence "+MainActivity.class.getSimpleName();


    private List<String> sessionUpdatesList = new ArrayList<String>();

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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
