package com.measurence.sdk.android.registration;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.measurence.sdk.android.demo.R;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This service register the device with the server. Does not check first if there is a registration id
 * already saved in the preferences, so that registration can be forced.
 */
public class RegistrationService extends IntentService {

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

        String apiSubscriptionsRegistryHost = getString(R.string.api_subscription_host);
        int  apiSubscriptionsRegistryPort = Integer.parseInt(getString(R.string.api_subscription_port));
        String apiSubscriptionRegistryPath = getString(R.string.api_subscription_path);
        String partnerId = getString(R.string.partner_id);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress().replace(":", "");

        //Log.i(TAG, "mac|" + macAddress + "|registration id|" + registrationId);
        // TODO: use Uri.builder with port. Add partner identity from gcm config
        // and user_identity as param
        // todo: add registration id
        // https://bitbucket.org/measurence/svc-api-subscription#markdown-header-svc-api-subscription-public-end-points
        URI apiSubscriptionURI = URI.create("http://" + apiSubscriptionsRegistryHost + ":"
                + apiSubscriptionsRegistryPort
                + apiSubscriptionRegistryPath + "/"
                + partnerId + "/"
                + user_identity + "/"
                + macAddress);
        InputStream apiSubscriptionRequestStream = null;
        try {
            apiSubscriptionRequestStream = apiSubscriptionURI.toURL().openStream();
            String result = IOUtils.toString(apiSubscriptionRequestStream);
            Log.i(LOG_TAG, "api subscription result|" + result);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(apiSubscriptionRequestStream);
        }
    }
}

