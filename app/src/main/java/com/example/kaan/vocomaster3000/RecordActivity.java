package com.example.kaan.vocomaster3000;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
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

    private SeekBar mProgress;
    private ToggleButton mRecordButton;
    private Button mPlayButton = null;
    private EditText mFileNameTextEntry;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private boolean mRecording = false;
    private boolean mPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mFileNameTextEntry = (EditText) findViewById(R.id.fileNameTextField);

        mProgress = (SeekBar) findViewById(R.id.recordBar);
        mProgress.setEnabled(false);

        mPlayButton = (Button) findViewById(R.id.recordPlayButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaying = !mPlaying;
                onPlay(mPlaying);
            }
        });

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
        if (start) {
            startRecording();
        } else {
            stopRecording();
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
        if (!mFileName.isEmpty() || mFileName != null) {
            mPlayer = new MediaPlayer();
            mPlayButton.setText("Stop");
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("File must have a name")
                    .setMessage("You must give the recording a name before it can be saved.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // nothing
                        }
                    }).show();
        }
    }

    private void stopPlaying() {
        mPlayButton.setText("Play");

        mPlayer.release();
        mPlayer = null;
        mPlaying = false;
    }

    private void startRecording() {
        if (mPlaying) {
            stopPlaying();
        }
        mRecordButton.setText("Stop");
        mPlayButton.setEnabled(false);

        // Make filename from textEntry
        String mFileName = mFileNameTextEntry.getText().toString();

        String filename = Environment.getExternalStorageDirectory().getAbsolutePath();
        filename += mFileName;

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mRecorder.setOutputFile(filename);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecordButton.setText("Record");
        mPlayButton.setEnabled(true);

        try {
            mRecorder.stop();
            mRecordings.add(mFileName);
        }
        catch (Exception e) {
            // Stop happened too fast, delete file
            String filename = Environment.getExternalStorageDirectory().getAbsolutePath();
            filename += mFileName;
            File lastRecording = new File(filename);
            lastRecording.delete();
        }
        mRecorder.release();
        mRecorder = null;
        mRecording = false;
    }
}
