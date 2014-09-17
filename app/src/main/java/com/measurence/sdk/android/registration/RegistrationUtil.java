package com.measurence.sdk.android.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.measurence.sdk.android.demo.R;


/**
 * Checks that there is a registration id saved in preferences
 * Created by chiara on 17/09/2014.
 */

public class RegistrationUtil {

    public static boolean checkRegistration(Context context) {
        final String LOG_TAG = context.getString(R.string.log_prefix) + " "+RegistrationUtil.class.getSimpleName();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String registrationId = preferences.getString(context.getString(R.string.prop_reg_id), null);
        if (registrationId == null) {
            return false;
        }

        int registeredVersion = preferences.getInt(context.getString(R.string.app_version), Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG_TAG, "App version changed.");
            return false;
        }

        return true;
    }
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}


