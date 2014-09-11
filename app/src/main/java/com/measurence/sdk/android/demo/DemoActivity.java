package com.measurence.sdk.android.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DemoActivity extends Activity {

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_IS_APP_SUBSCRIBED_TO_MEASURENCE_API = "is_app_subscribed_to_measurence_api";
    private static final String PROPERTY_APP_VERSION = "0.1-SNAPSHOT";
    private final static String SENDER_ID = "1093814696074";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private final static String TAG = "MeasurenceSDKDemo";

    private GoogleCloudMessaging gcm;

    Context context;
    String registrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        context = getApplicationContext();
        if (checkPlayServices()) {
            registrationId = getRegistrationId(context);
            if (registrationId.isEmpty()) registerInBackground();

            if (!isAppSubscribedToMeasurenceApi(context)) applyToApiSubscription();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected void onPostExecute(Object o) {
                Log.i(TAG, o + "\n");
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    registrationId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + registrationId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    applyToApiSubscription();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, registrationId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void storeAppSubscriptionToMeasurenceApi(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving appSubscriptionToMeasurenceApi on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_IS_APP_SUBSCRIBED_TO_MEASURENCE_API, true);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private boolean isAppSubscribedToMeasurenceApi(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        boolean isAppSubscribedToMeasurenceApi = prefs.getBoolean(PROPERTY_IS_APP_SUBSCRIBED_TO_MEASURENCE_API, false);
        if (!isAppSubscribedToMeasurenceApi) return false;

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "app was subscribed to Measurence apis but a new registration id has been issued given to app version change");
            return false;
        }

        return true;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void applyToApiSubscription() {

        // [TODO] has to be removed ASAP
        Boolean applyToSubscriptionEnabled = true;
        Log.i(TAG, "apply to api subscription|enabled|" + applyToSubscriptionEnabled);
        if (!applyToSubscriptionEnabled) return;

        //String apiSubscriptionsRegistryHost = "10.0.3.2";
        String apiSubscriptionsRegistryHost = "192.168.1.203";
        int  apiSubscriptionsRegistryPort = 10082;

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress().replace(":", "");

        Log.i(TAG, "mac|" + macAddress + "|registration id|" + registrationId);

        // ------------
        // [TODO] to be removed and properly handled
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // ------------

        String partnerId = "example_partner";
        String userIdentity = "example_identity";

        URI apiSubscriptionURI = getAndroidApplySubscriptionURI(apiSubscriptionsRegistryHost, apiSubscriptionsRegistryPort, macAddress, partnerId, userIdentity);
        //URI apiSubscriptionURI = getHttpPostApplySubscriptionURI(apiSubscriptionsRegistryHost, apiSubscriptionsRegistryPort, macAddress, partnerId, userIdentity);
        Log.i(TAG, "calling|" + apiSubscriptionURI);

        InputStream apiSubscriptionRequestStream = null;
        try {
            apiSubscriptionRequestStream = apiSubscriptionURI.toURL().openStream();
            String result = IOUtils.toString(apiSubscriptionRequestStream);
            Log.i(TAG, "api subscription result|" + result);
            storeAppSubscriptionToMeasurenceApi(context);
        } catch (IOException e) {
            Log.i(TAG, "api subscription error occurred|" + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(apiSubscriptionRequestStream);
        }
    }

    private URI getHttpPostApplySubscriptionURI(String apiSubscriptionsRegistryHost, int apiSubscriptionsRegistryPort, String macAddress, String partnerId, String userIdentity) {
        return URI.create("http://" +
                apiSubscriptionsRegistryHost + ":" + apiSubscriptionsRegistryPort + "/api/http_post/apply_subscription/" +
                partnerId + "/" + userIdentity + "/" + macAddress);
    }

    private URI getAndroidApplySubscriptionURI(String apiSubscriptionsRegistryHost, int apiSubscriptionsRegistryPort, String macAddress, String partnerId, String userIdentity) {
        return URI.create("http://" +
                apiSubscriptionsRegistryHost + ":" + apiSubscriptionsRegistryPort + "/api/android/apply_subscription/" +
                partnerId + "/" + userIdentity + "/" + registrationId + "/" + macAddress);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(DemoActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
}
