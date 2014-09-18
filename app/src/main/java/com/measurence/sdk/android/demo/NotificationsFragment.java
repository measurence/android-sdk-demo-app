package com.measurence.sdk.android.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.demo.dummy.DummyContent;
import com.measurence.sdk.android.service.NotificationService;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * interface.
 */
public class NotificationsFragment extends android.support.v4.app.Fragment implements AbsListView.OnItemClickListener {

    public String LOG_TAG = "MeasurenceSDK " + NotificationsFragment.class.getSimpleName();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<String> mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>()) {
        };

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            private void displaySessionUpdate(PresenceSessionUpdate presenceSessionUpdate) {
                mAdapter.add(presenceSessionUpdate.toString());
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver), new IntentFilter(NotificationService.SESSION_UPDATE_INTENT_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        setEmptyText(getString(R.string.notifications_empty_text));

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }
}

