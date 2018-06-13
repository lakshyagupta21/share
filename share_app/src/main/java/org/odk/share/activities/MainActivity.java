package org.odk.share.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.odk.share.R;
import org.odk.share.application.Share;
import org.odk.share.dao.InstancesDao;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.dto.ShareInstance;
import org.odk.share.preferences.SettingsPreference;
import org.odk.share.provider.InstanceProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.bSendForms) Button sendForms;
    @BindView(R.id.bViewWifi) Button viewWifi;

    private ShareDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.send_forms));
        setSupportActionBar(toolbar);

        Share.createODKDirs();
//        databaseHelper = new ShareDatabaseHelper();



//        Timber.d("ID : " + databaseHelper.insertInstance(instance));

        ContentValues values = new ContentValues();
        values.put(DISPLAY_NAME, "displayName");
        values.put(STATUS, InstanceProviderAPI.STATUS_COMPLETE);
        values.put(CAN_EDIT_WHEN_COMPLETE, "true");
        values.put(SUBMISSION_URI, "submissionUri");
        values.put(INSTANCE_FILE_PATH, Environment.getExternalStorageDirectory() + "/" + "path");
        values.put(JR_FORM_ID, "formId");
        values.put(JR_VERSION, "formVersion");
        Timber.d("Content "+ values);
        Uri uri = new InstancesDao().saveInstance(values);
        Timber.d(uri + " " + uri.getLastPathSegment());
//
//        ShareInstance instance = new ShareInstance();
//        instance.setInstanceId(Long.parseLong(uri.getLastPathSegment()));
//        instance.setInstructions("Instru");
//        instance.setLastStatusChangeDate((long) 123);
//        instance.setReviewed(true);
//        instance.setTransferStatus("sent");
    }

    @OnClick (R.id.bViewWifi)
    public void viewWifiNetworks() {
        Intent intent = new Intent(this, WifiActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.bSendForms)
    public void selectForms() {
        Intent intent = new Intent(this, InstancesList.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsPreference.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}