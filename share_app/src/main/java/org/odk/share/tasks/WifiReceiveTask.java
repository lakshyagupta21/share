package org.odk.share.tasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import org.odk.share.dao.FormsDao;
import org.odk.share.dao.InstancesDao;
import org.odk.share.database.ShareDatabaseHelper;
import org.odk.share.dto.ShareInstance;
import org.odk.share.listeners.ProgressListener;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.share.dto.ShareInstance.INSTANCE_ID;
import static org.odk.share.dto.ShareInstance.INSTRUCTIONS;
import static org.odk.share.dto.ShareInstance.LAST_STATUS_CHANGE_DATE;
import static org.odk.share.dto.ShareInstance.REVIEWED;
import static org.odk.share.dto.ShareInstance.TRANSFER_STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.share.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;

/**
 * Created by laksh on 5/31/2018.
 */

public class WifiReceiveTask extends AsyncTask<String, Integer, String> {

    private String ip;
    private int port;
    private ProgressListener stateListener;
    private DataInputStream dis;
    private DataOutputStream dos;
    private int total;
    private int progress;

    private static final String INSTANCE_PATH = "share/instances/";
    private static final String FORM_PATH = "share/forms/";

    public void setUploaderListener(ProgressListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.progressUpdate(values[0], values[1]);
            }
        }
    }

    public WifiReceiveTask(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String receiveForms() {
        Socket socket = null;
        Timber.d("Socket " + ip + " " + port);

        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
            total = dis.readInt();
            int num = dis.readInt();
            Timber.d("Number of forms" + num + " ");
            while (num-- > 0) {
                Timber.d("Reading form");
                if (readFormAndInstances()) {
                    return String.valueOf(progress);
                }
            }
        } catch (UnknownHostException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }

        return  String.valueOf(progress);
    }

    private boolean readFormAndInstances() {
        try {

            Timber.d("readFormAndInstances");
            String formId = dis.readUTF();
            String formVersion = dis.readUTF();
            Timber.d(formId + " " + formVersion);
            if (formVersion.equals("-1")) {
                formVersion = null;
            }

            boolean formExists = isFormExits(formId, formVersion);
            Timber.d("Form exits " + formExists);
            dos.writeBoolean(formExists);

            if (!formExists) {
                // read form
                readForm();
            }

            // readInstances
            readInstances(formId, formVersion);
            return true;
        } catch (IOException e) {
            Timber.e(e);
        }
        return false;
    }

    private boolean isFormExits(String formId, String formVersion) {
        String []selectionArgs;
        String selection;

        if (formVersion == null) {
            selectionArgs = new String[]{formId};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{formId, formVersion};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + "=?";
        }

        Cursor cursor = new FormsDao().getFormsCursor(null, selection, selectionArgs, null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

    private void readForm() {
        try {
            String displayName = dis.readUTF();
            String formId = dis.readUTF();
            String formVersion = dis.readUTF();
            String submissionUri = dis.readUTF();

            if (formVersion.equals("-1")) {
                formVersion = null;
            }

            if (submissionUri.equals("-1")) {
                submissionUri = null;
            }

            Timber.d(displayName + " " + formId + " " + formVersion + " " + submissionUri);
            String filename = receiveFile(FORM_PATH);
            int numOfRes = dis.readInt();
            while (numOfRes-- > 0) {
                receiveFile(FORM_PATH + displayName + "-media");
            }

            // Add row in forms.db
            ContentValues values = new ContentValues();
            values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, displayName);
            values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formId);
            values.put(FormsProviderAPI.FormsColumns.JR_VERSION, formVersion);
            values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FORM_PATH + filename );
            values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, submissionUri);
            values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, FORM_PATH + displayName + "-media");
            new FormsDao().saveForm(values);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void readInstances(String formId, String formVersion) {
        try {
            int numInstances = dis.readInt();
            while (numInstances-- > 0) {
                publishProgress(++progress, total);
                String displayName = dis.readUTF();
                String submissionUri = dis.readUTF();

                if (submissionUri.equals("-1")) {
                    submissionUri = null;
                }

                int numRes = dis.readInt();
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS",
                        Locale.ENGLISH).format(Calendar.getInstance().getTime());
                String path = INSTANCE_PATH + formId + "_" + time;
                while (numRes-- > 0) {
                    receiveFile(path);
                }

                // Add row in instances table
                ContentValues values = new ContentValues();
                values.put(DISPLAY_NAME, displayName);
                values.put(STATUS, InstanceProviderAPI.STATUS_COMPLETE);
                values.put(CAN_EDIT_WHEN_COMPLETE, "true");
                values.put(SUBMISSION_URI, submissionUri);
                values.put(INSTANCE_FILE_PATH, Environment.getExternalStorageDirectory() + "/" + path);
                values.put(JR_FORM_ID, formId);
                values.put(JR_VERSION, formVersion);
                Timber.d("Content " + values);
                Uri uri = new InstancesDao().saveInstance(values);
                Timber.d(uri + " " + uri);

                // Add row in share table
                ContentValues shareValues = new ContentValues();
                shareValues.put(INSTANCE_ID, Long.parseLong(uri.getLastPathSegment()));
                shareValues.put(TRANSFER_STATUS, "receive");
                new ShareDatabaseHelper().insertInstance(shareValues);
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private String receiveFile(String path) {
        String filename = null;
        try {
            filename = dis.readUTF();
            long fileSize = dis.readLong();
            Timber.d("Size of file " + filename + " " + fileSize);
            File shareDir = new File(Environment.getExternalStorageDirectory(), path);

            if (!shareDir.exists()) {
                Timber.d("Directory created " + shareDir.getPath() + " " + shareDir.mkdirs());
            }

            File newFile = new File(shareDir, filename);
            newFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(newFile);
            int n;
            byte[] buf = new byte[4096];
            while (fileSize > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                fos.write(buf, 0, n);
                fileSize -= n;
            }
            fos.close();
            Timber.d("File created and saved " + newFile.getAbsolutePath() + " " + newFile.getName());
        } catch (IOException e) {
            Timber.e(e);
        }
        return filename;
    }

    @Override
    protected String doInBackground(String... strings) {
        return receiveForms();
    }

    @Override
    protected void onPostExecute(String s) {
        stateListener.uploadingComplete(s);
    }

}
