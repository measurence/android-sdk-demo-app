package com.measurence.sdk.android.demo;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String apiSubscriptionsRegistryHost = "localhost";
        int  apiSubscriptionsRegistryPort = 10082;

//        PartnerAccount partnerAccount = new PartnerAccount("example_partner");
//        UserIdentity userIdentity = new UserIdentity("example_identity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress().replace(":", "");

        TextView textMessages = (TextView)findViewById(R.id.textMessages);
        textMessages.setText("Your Mac Address:" + macAddress);

        // ------------
        // [TODO] to be removed and properly handled
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // ------------



        URI apiSubscriptionURI = URI.create("http://10.0.3.2:10082/api/apply_subscription_with_mac/example_partner/example_identity/" + macAddress);
        InputStream apiSubscriptionRequestStream = null;
        try {
            apiSubscriptionRequestStream = apiSubscriptionURI.toURL().openStream();
            IOUtils.toString(apiSubscriptionRequestStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(apiSubscriptionRequestStream);
        }

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
}
