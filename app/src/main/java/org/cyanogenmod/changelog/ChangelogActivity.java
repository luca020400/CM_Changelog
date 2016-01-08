package org.cyanogenmod.changelog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Copyright (c) 2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ChangelogActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static String TAG = "ChangelogActivity";
    /**
     * View Container
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    /**
     * RecyclerView used to list all the changes
     */
    private RecyclerView mRecyclerView;
    /**
     * Adapter for the RecyclerView
     */
    private ChangelogAdapter mAdapter;
    /**
     * LayoutManager of the RecyclerView
     */
    private RecyclerView.LayoutManager mLayoutManager;
    /**
     * Set of models used as data source
     */
    private ArrayList<Change> mList;
    /**
     * String representing the full CyanogenMod build version
     */
    private String mCMVersion;
    /**
     * String representing the CyanogenMod version of the device (e.g 13)
     */
    private String mCyanogenMod;
    /**
     * String representing the update channel aka release type (e.g nightly)
     */
    private String mCMReleaseType;
    /**
     * String representing device code-name (e.g. hammerhead)
     */
    private String mDevice;
    /**
     * API URL
     */
    private final String API_URL = "http://api.cmxlog.com/changes";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        retrieveDeviceInfo();
        init();
        updateChangelog();
    }

    /**
     * Utility method.
     */
    private void init(){
        // Init list
        mList = new ArrayList<>();
        // Setup SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Color scheme of the refresh spinner
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary_dark, R.color.color_accent);
        // Setup RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Setup divider for RecyclerView itemsi
        mRecyclerView.addItemDecoration(new Divider(this));
        // Init adapter
        mAdapter = new ChangelogAdapter(mList);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.device_info:
                deviceInfoDialog();
                break;
            case R.id.menu_refresh:
                updateChangelog();
                break;
        }

        return true;
    }

    /**
     * Retrieve info about the device.
     */
    private void retrieveDeviceInfo() {
        mCMVersion = Cmd.exec("getprop ro.cm.version");
        String[] version = mCMVersion.split("-");
        mCyanogenMod = version[0].replace(".0", "");
        mCMReleaseType = version[2];
        mDevice = version[3];
    }

    /**
     * Crate a dialog displaying info about the device.
     */
    private void deviceInfoDialog() {
        String message = String.format("%s %s\n%s %s\n%s %s",
                getString(R.string.device_info_device), mDevice,
                getString(R.string.device_info_version), mCMVersion,
                getString(R.string.device_info_update_channel), mCMReleaseType);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.device_info)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Small);
    }


    @Override
    public void onRefresh() {
        updateChangelog();
    }

    /**
     * Fetch data asynchronously
     */
    private void updateChangelog() {
        Log.i(TAG, "Updating Changelog");
        String apiUrl = String.format("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice);
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.w(TAG, "Missing network connection");
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        new AsyncTask<String, String, String>() {

            @Override
            protected void onPreExecute() {
                if (mAdapter != null) mAdapter.clear();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected String doInBackground(String... urls) {
                try {
                    String scanner =
                            new Scanner(new URL(urls[0]).openStream(), "UTF-8").useDelimiter("\\A").next();
                    JSONArray jsonArray = new JSONArray(scanner);

                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String subject = (String) jsonObject.get("subject");
                        String project = (String) jsonObject.get("project");
                        String lastUpdated = (String) jsonObject.get("last_updated");
                        String id = jsonObject.get("id").toString();
                        mList.add(new Change(subject, project, lastUpdated, id));
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "", e);
                }

                Log.i(TAG, "Successfully parsed CMXLog API");

                return null;
            }

            @Override
            protected void onPostExecute(String urls) {
                mAdapter.addAll(mList);
                mAdapter.notifyDataSetChanged();
                // delay refreshing animation just for the show
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 400);
            }
        }.execute(apiUrl);
    }

}
