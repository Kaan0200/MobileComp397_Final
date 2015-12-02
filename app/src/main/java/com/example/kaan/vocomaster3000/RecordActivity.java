package com.example.kaan.vocomaster3000;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

public class RecordActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private ArrayList<String> mRecordings;

    private EditText mFileNameTextBox;
    private ToggleButton mRecordButton;
    private Button mPlayButton = null;
    private Button mReturnButton;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private boolean mRecording = false;
    private boolean mPlaying = false;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        verifyStoragePermissions(this);

        mPlayButton = (Button) findViewById(R.id.recordPlayButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaying = !mPlaying;
                onPlay(mPlaying);
            }
        });

        mReturnButton = (Button) findViewById(R.id.returnButton);
        mReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent output = new Intent();
                output.putStringArrayListExtra("recordings", mRecordings);
                setResult(RESULT_OK, output);
                finish();
            }
        });

        mFileNameTextBox = (EditText) findViewById(R.id.fileNameTextBox);

        mRecordButton = (ToggleButton) findViewById(R.id.recordToggleButton);
        mRecordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRecording = isChecked;
                onRecord(mRecording);
            }
        });

        mRecordings = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();  // Always call the superclass method first

        mRecordings = new ArrayList<>();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pass over list of recording file names
        Intent output = new Intent();
        output.putStringArrayListExtra("recordings", mRecordings);
        setResult(RESULT_OK, output);
    }

    private void onRecord(boolean start) {
        if (mFileNameTextBox.getText().toString().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("You must give this file a name")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mRecording = false;
                            mRecordButton.setChecked(false);
                        }
                    }).show();
            stopRecording();
        } else {
            if (start) {
                startRecording();
            } else {
                stopRecording();
            }
        }

    }

    private void onPlay(boolean start) {
            if (start) {
                startPlaying();
            } else {
                stopPlaying();
            }
    }

    private void startPlaying() {
        if (mFileName != null) {
            mPlayer = new MediaPlayer();
            mPlayButton.setText("Stop");
            try {
                mPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/FinalProj/" + mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void stopPlaying() {
        mPlayButton.setText("Play");

        mPlayer.release();
        mPlayer = null;
        mPlaying = false;
    }

    private void startRecording() {
        if (mFileNameTextBox.getText().toString().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("You must give this file a name")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // nothing
                        }
                    }).show();
        }

        if (mPlaying) {
            stopPlaying();
        }
        mRecordButton.setText("Stop");
        mPlayButton.setEnabled(false);

        // Make filename from timestamp
        mFileName = mFileNameTextBox.getText().toString() + ".3gp";      // 20151123-15:59:48

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FinalProj/";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        String file = path + mFileName;

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setOutputFile(file);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void stopRecording() {
        Log.i("REC", "Stopped recording");
        mRecordButton.setText("Record");
        mPlayButton.setEnabled(true);

        try {
            mRecorder.stop();
            mRecordings.add(mFileName);
        }
        catch (Exception e) {
            // Stop happened too fast, delete file
            String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FinalProj/";
            filename += mFileName;
            File lastRecording = new File(filename);
            lastRecording.delete();
        }
        mRecorder.release();
        mRecorder = null;
        mRecording = false;
    }
}
