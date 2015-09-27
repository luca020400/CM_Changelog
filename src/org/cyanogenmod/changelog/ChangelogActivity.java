package org.cyanogenmod.changelog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.cyanogenmod.changelog.misc.ChangelogTask;
import org.cyanogenmod.changelog.misc.Cmd;

public class ChangelogActivity extends Activity {
    public static ChangelogActivity _instance;
    public SwipeRefreshLayout swipeRefreshLayout;

    private String mCMVersion;
    private String mCyanogenMod;
    private String mCMReleaseType;
    private String mDevice;
    private int mReleases = 4;
    private String[] mOfficials = new String[mReleases];
    private boolean mUnofficial;

    public void onCreate(Bundle savedInstanceState) {
        _instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCMVersion = Cmd.exec("getprop ro.cm.version");
        String[] version = mCMVersion.split("-");
        mCyanogenMod = version[0];
        mCMReleaseType = version[2];
        mDevice = Cmd.exec("getprop ro.product.name");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UpdateChangelog();
            }});
        populateOfficial();
        alertUnofficial(mCMReleaseType);
        UpdateChangelog();
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
                DeviceInfo();
                break;
        }

        return true;
    }

public void populateOfficial() {
    /*
     * Add new official releases here, also remember to update the array
     */
    mOfficials[0] = "NIGHTLY";
    mOfficials[1] = "YOG4P";
    mOfficials[2] = "YNG4N";
    mOfficials[3] = "XNG3C";
}

    public void DeviceInfo() {
        String message = String.format("%s %s\n\n%s %s\n\n%s %s",
                getString(R.string.device_info_device), mDevice,
                getString(R.string.device_info_running), mCMVersion,
                getString(R.string.device_info_update_channel), mCMReleaseType);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.device_info)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Small);
    }

    public void alertUnofficial(String version) {
        int mshowunofficial = 0;
        for (int i = 0; i < mReleases; i++){
            if (version == mOfficials[i])
                mshowunofficial++;
        }
        if (mshowunofficial <= 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.unofficial_info)
                    .setMessage(R.string.unofficial_mesasge)
                    .setPositiveButton(R.string.dialog_ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
        }
    }

    public void UpdateChangelog() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected()) {
            Toast.makeText(this, R.string.data_connection_required, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        new ChangelogTask().execute(String.format
                ("http://api.cmxlog.com/changes/%s/%s", mCyanogenMod, mDevice));
    }

    public static ChangelogActivity getInstance() {
        return _instance;
    }

    public static class Change {
        public String subject_adapter;
        public String project_adapter;
        public String last_updated_adapter;

        public Change(String subject_adapter, String project_adapter, String last_updated_adapter) {
            this.subject_adapter = subject_adapter;
            this.project_adapter = project_adapter;
            this.last_updated_adapter = last_updated_adapter;
        }
    }
}
