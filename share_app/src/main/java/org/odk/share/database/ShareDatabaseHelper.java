package org.odk.share.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.share.application.Share;
import org.odk.share.dto.ShareInstance;
import org.odk.share.provider.InstanceProviderAPI;

import timber.log.Timber;

import static org.odk.share.dto.ShareInstance.ID;
import static org.odk.share.dto.ShareInstance.INSTANCE_ID;
import static org.odk.share.dto.ShareInstance.INSTRUCTIONS;
import static org.odk.share.dto.ShareInstance.LAST_STATUS_CHANGE_DATE;
import static org.odk.share.dto.ShareInstance.REVIEWED;
import static org.odk.share.dto.ShareInstance.TRANSFER_STATUS;

/**
 * Created by laksh on 6/10/2018.
 */

public class ShareDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "share.db";
    private static final String SHARE_TABLE_NAME = "share";

    private static final int DATABASE_VERSION = 4;

    public ShareDatabaseHelper() {
        super(new DatabaseContext(Share.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
        Timber.d("Context " + Share.METADATA_PATH);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("Database creation started");
        createInstancesTable(db);
        Timber.d("Database creation stopped");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Timber.d("onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + SHARE_TABLE_NAME);
        onCreate(db);
    }

    private void createInstancesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SHARE_TABLE_NAME + " ("
                + ID + " integer primary key, "
                + REVIEWED + " boolean, "
                + INSTRUCTIONS + " text, "
                + INSTANCE_ID + " integer not null, "
                + TRANSFER_STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null ); ");

    }

    public long insertInstance(ShareInstance instance) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(REVIEWED, instance.getReviewed());
        values.put(INSTRUCTIONS, instance.getInstructions());
        values.put(INSTANCE_ID, instance.getInstanceId());
        values.put(TRANSFER_STATUS, instance.getTransferStatus());
        values.put(LAST_STATUS_CHANGE_DATE, instance.getLastStatusChangeDate());

        long id = sqLiteDatabase.insert(SHARE_TABLE_NAME, null, values);
        sqLiteDatabase.close();

        return id;
    }

    public long insertInstance(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        if (!values.containsKey(REVIEWED)) {
            values.put(REVIEWED, false);
        }

        Long now = System.currentTimeMillis();

        if (!values.containsKey(LAST_STATUS_CHANGE_DATE)) {
            values.put(LAST_STATUS_CHANGE_DATE, now);
        }
        return sqLiteDatabase.insert(SHARE_TABLE_NAME, null, values);
    }
}
