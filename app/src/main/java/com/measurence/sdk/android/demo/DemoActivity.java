package com.measurence.sdk.android.demo;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.measurence.api_subscription.ApiSubscription;
import com.measurence.api_subscription.ApiSubscriptionsRegistry;
import com.measurence.api_subscription.PartnerAccount;
import com.measurence.api_subscription.impl.ApiSubscriptionRestImpl;
import com.measurence.identity_discovery.UserIdentity;

import scala.Some;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String apiSubscriptionsRegistryHost = "localhost";
        int  apiSubscriptionsRegistryPort = 10082;

        PartnerAccount partnerAccount = new PartnerAccount("example_partner");
        UserIdentity userIdentity = new UserIdentity("example_identity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress();

        TextView textMessages = (TextView)findViewById(R.id.textMessages);
        textMessages.setText("Your Mac Address:" + macAddress);

        ApiSubscriptionsRegistry apiSubscriptionsRegistry = new ApiSubscriptionRestImpl(apiSubscriptionsRegistryHost, apiSubscriptionsRegistryPort);
        ApiSubscription apiSubscription = new ApiSubscription(partnerAccount, userIdentity, new Some<String>(macAddress));
        apiSubscriptionsRegistry.apply(apiSubscription);
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
