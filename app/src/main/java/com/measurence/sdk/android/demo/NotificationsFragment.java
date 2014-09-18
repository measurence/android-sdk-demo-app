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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.measurence.sdk.android.PresenceSessionUpdate;
import com.measurence.sdk.android.UserIdentity;
import com.measurence.sdk.android.gcm_push_notifications.PresenceSessionUpdatesNotificationService;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private ArrayAdapter<PresenceSessionUpdate> mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setHasOptionsMenu(true);

            mAdapter = new ArrayAdapter<PresenceSessionUpdate>(getActivity(),
                    R.layout.list_item_notification, R.id.list_item_notification_view, new ArrayList<PresenceSessionUpdate>()) {

                DateFormat dateFormat = DateFormat.getDateTimeInstance();

                private void setText(View root, int id, String text) {
                    TextView textView = (TextView) root.findViewById(id);
                    textView.setText(text);
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View itemView = inflater.inflate(R.layout.list_item_notification, null);
                    PresenceSessionUpdate item = getItem(position);
                    // Not the best, we should have a notification date. WIP
                    String now = dateFormat.format(item.getInterval().getEnd());
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

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

                private void displaySessionUpdate(PresenceSessionUpdate presenceSessionUpdate) {
                    mAdapter.insert(presenceSessionUpdate, 0);
                    Log.i(LOG_TAG, "displaying session update|" + presenceSessionUpdate);
                }

                @Override
                public void onReceive(Context context, Intent intent) {
                    String presenceSessionUpdateJson = intent.getStringExtra(PresenceSessionUpdatesNotificationService.SESSION_UPDATE_JSON_PARAMETER);

                    displaySessionUpdate(PresenceSessionUpdate.fromJson(presenceSessionUpdateJson));

                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver), new IntentFilter(PresenceSessionUpdatesNotificationService.SESSION_UPDATE_INTENT_ID));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear) {
            mAdapter.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity(). getMenuInflater().inflate(R.menu.notifications, menu);
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

