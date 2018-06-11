package org.odk.share.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.odk.share.R;
import org.odk.share.application.Share;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.dto.ShareInstance;
import org.odk.share.preferences.SettingsPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

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
        databaseHelper = new ShareDatabaseHelper();

        ShareInstance instance = new ShareInstance();
        instance.setInstanceId((long) 1);
        instance.setInstructions("Instru");
        instance.setLastStatusChangeDate((long) 123);
        instance.setReviewed(true);
        instance.setTransferStatus("sent");

        Timber.d("ID : " + databaseHelper.insertInstance(instance));
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