package com.example.kaan.vocomaster3000;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MachineActivity extends AppCompatActivity {

    // fill the ListViews with String Arrays
    public Spinner mInstrumentSelector;
    public Spinner mInstrumentTypeSelector;
    public Spinner mParameterSelector;
    public SeekBar mBPMSeekBar;
    public SeekBar mInstrumentVolumeSeekBar;
    public TextView mBPMTextView;
    public TextView mInstrumentVolumeTextView;
    public TextView mTotalTimerTextView;
    public TextView mBeatCountTextView;
    public TextView mSectionCountTextView;
    public TextView mLoopTimerTextView;
    public ToggleButton mPlayToggle;
    public Button mNewCustomButton;

    public int mSelectedKick;
    public int mSelectedHat;
    public int mSelectedClap;
    public int mSelectedCustom;
    public String mSelectedKickType = "trance";
    public String mSelectedClapType = "clap1";
    public String mSelectedHatType  = "loose";
    public String mSelectedCustomType; //TODO: if none, then don't
    public int mSelectedKickVolume   = 10;
    public int mSelectedClapVolume   = 10;
    public int mSelectedHatVolume    = 10;
    public int mSelectedCustomVolume = 10;

    // variable to hold which was the last selected line, for saving
    // default: kick
    public String lastLine = "Kick";
    //The booleans for effect layers
    public boolean[] kickLine = new boolean[16];
    public boolean[] clapLine = new boolean[16];
    public boolean[] cymbalLine = new boolean[16];
    public boolean[] customLine = new boolean[16];

    // Toggles for beats
     public ToggleButton beat1;
    public ToggleButton beat2;
    public ToggleButton beat3;
    public ToggleButton beat4;
     public ToggleButton beat5;
    public ToggleButton beat6;
    public ToggleButton beat7;
    public ToggleButton beat8;
     public ToggleButton beat9;
    public ToggleButton beat10;
    public ToggleButton beat11;
    public ToggleButton beat12;
     public ToggleButton beat13;
    public ToggleButton beat14;
    public ToggleButton beat15;
    public ToggleButton beat16;

    public SoundPool mSoundPool;
    public int kickSoundId;
    public int clapSoundId;
    public int hhatSoundId;
    //TODO: loaded custom sound ID

    // this is the timers and variables for the timer thread
    public final Handler mHandle = new Handler();
    public long startTime = 0;
    public int movingSectTime = 0;
    // time for 2 measures
    public double loopTime = 0;
    public double beatTime = 0;
    public double sectTime = 0;
    public Runnable mTimerRunner = new Runnable() {
        @Override
        public void run() {

            // this is the real time to account for delay
            final double totalTime = (System.currentTimeMillis() - startTime)/1000.0;
            // update text views
            final double movingLoopTime = totalTime % loopTime;
            final int temptSect = (int) (movingLoopTime / sectTime) + 1;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSectionNumber(temptSect);

                    mLoopTimerTextView.setText(String.format("%.3f", movingLoopTime));
                    mBeatCountTextView.setText((int) (totalTime / beatTime) + "");
                    mTotalTimerTextView.setText(String.format("%.3f", totalTime));
                    mSectionCountTextView.setText(movingSectTime + "/16");
                }
            });

            mHandle.postDelayed(this, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);

        // get the custom button
        mNewCustomButton = (Button) findViewById(R.id.newCustomButton);
        // get the spinners
        mInstrumentSelector = (Spinner) findViewById(R.id.instrumentSelectorSpinner);
        mInstrumentTypeSelector = (Spinner) findViewById(R.id.instrumentTypeSpinner);
        // get the slider, set initial value
        mBPMSeekBar = (SeekBar) findViewById(R.id.bpmSeekbar);
        mBPMSeekBar.setProgress(38);
        // get the volume slider, set initial value
        mInstrumentVolumeSeekBar = (SeekBar) findViewById(R.id.InstrumentVolumeSeekbar);
        mInstrumentVolumeSeekBar.setProgress(10);
        // get thmBPMTextViewview, set text
        mBPMTextView = (TextView) findViewById(R.id.bpmText);
        mBPMTextView.setText("BPM: 128");
        mTotalTimerTextView = (TextView) findViewById(R.id.totalTimerText);
        mBeatCountTextView = (TextView) findViewById(R.id.beatCountText);
        mSectionCountTextView = (TextView) findViewById(R.id.sectionNumberText);
        mLoopTimerTextView = (TextView) findViewById(R.id.loopTimeTextView);
        mInstrumentVolumeTextView = (TextView) findViewById(R.id.InstrumentVolumeTextView);
        // get all the toggles
        findAllBeatToggleButtons();
        mPlayToggle = (ToggleButton) findViewById(R.id.playToggle);

        // create the adapter for the instrument selector
        ArrayAdapter<String> instrumentAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.instruments));
        mInstrumentSelector.setAdapter(instrumentAdapter);
        // create the adapter for the parameter selector
        ArrayAdapter<String> parameterAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.param_length));
        // create adapters for the instrument types
        final ArrayAdapter<String> kickAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.kick_type));
        final ArrayAdapter<String> clapAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.clap_type));
        final ArrayAdapter<String> cymbAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.highhat_type));


        // set up the on change listener for the play button
        mPlayToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b == false) { // STOP
                    mHandle.removeCallbacks(mTimerRunner);
                    if (mInstrumentSelector.getSelectedItem().toString() == "Custom"){
                        mNewCustomButton.setEnabled(true);
                    }
                } else { // PLAY
                    // disable buttons
                    mNewCustomButton.setEnabled(false);
                    mInstrumentVolumeSeekBar.setEnabled(false);
                    mBPMSeekBar.setEnabled(false);
                    // save the lines
                    switch (mInstrumentSelector.getSelectedItem().toString()) {
                        case "Kick":
                            kickLine = SaveToggleLine();
                            mSelectedKick = mInstrumentTypeSelector.getSelectedItemPosition();
                            break;
                        case "Clap":
                            clapLine = SaveToggleLine();
                            mSelectedClap = mInstrumentTypeSelector.getSelectedItemPosition();
                            break;
                        case "Highhat":
                            cymbalLine = SaveToggleLine();
                            mSelectedHat = mInstrumentTypeSelector.getSelectedItemPosition();
                            break;
                        case "Custom":
                            customLine = SaveToggleLine();
                            mSelectedCustom = mInstrumentTypeSelector.getSelectedItemPosition();
                            break;
                    }

                    // load the selected sounds into the pool
                    LoadSounds_AwaitCompletion();

                    // reset the timer
                    startTime = System.currentTimeMillis();
                    //time = 0;

                    // calculate the time for two measures
                    loopTime = 1.0 / ((((double) mBPMSeekBar.getProgress() + 90.0) / 8.0) / 60);
                    beatTime = loopTime / 8.0;
                    sectTime = loopTime / 16.0;

                    // start counting
                    mHandle.postDelayed(mTimerRunner, 0);
                }
            }
        });

        // set up the on change listener for the instrument spinner
        mInstrumentSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 1.
                // Save the last Line into its intArray, and the selected type
                try {
                    switch (lastLine) {
                        case "Kick":
                            kickLine = SaveToggleLine();
                            mSelectedKick = mInstrumentTypeSelector.getSelectedItemPosition();
                            mSelectedKickType = mInstrumentTypeSelector.getSelectedItem().toString();
                            mNewCustomButton.setEnabled(false);
                            break;
                        case "Clap":
                            clapLine = SaveToggleLine();
                            mSelectedClap = mInstrumentTypeSelector.getSelectedItemPosition();
                            mSelectedClapType = mInstrumentTypeSelector.getSelectedItem().toString();
                            mNewCustomButton.setEnabled(false);
                            break;
                        case "Highhat":
                            cymbalLine = SaveToggleLine();
                            mSelectedHat = mInstrumentTypeSelector.getSelectedItemPosition();
                            mSelectedHatType = mInstrumentTypeSelector.getSelectedItem().toString();
                            mNewCustomButton.setEnabled(false);
                            break;
                        case "Custom":
                            customLine = SaveToggleLine();
                            mSelectedCustom = mInstrumentTypeSelector.getSelectedItemPosition();
                            mSelectedCustomType = mInstrumentTypeSelector.getSelectedItem().toString();
                            mNewCustomButton.setEnabled(true);
                            break;
                    }
                } catch (NullPointerException e) {
                    // do nothing since this will always get tripped
                    // when starting up the application
                }
                // 2.
                // clean line and set into new values
                lastLine = (String) mInstrumentSelector.getSelectedItem();

                // 3.
                // last line is now current, so load in the appropriate intArray
                switch (lastLine) {
                    case "Kick":
                        // save into previous value
                        SetToggleLine(kickLine);
                        SetInstrumentType(mSelectedKick);
                        mInstrumentTypeSelector.setAdapter(kickAdapter);
                        break;
                    case "Clap":
                        SetToggleLine(clapLine);
                        mInstrumentTypeSelector.setAdapter(clapAdapter);
                        break;
                    case "Highhat":
                        SetToggleLine(cymbalLine);
                        mInstrumentTypeSelector.setAdapter(cymbAdapter);
                        break;
                    case "Custom":
                        SetToggleLine(customLine);
                        mNewCustomButton.setEnabled(true);
                        //TODO: create and handle adapter for custom sounds
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { /*Nothing*/ }
        });

        mInstrumentTypeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (mInstrumentSelector.getSelectedItem().toString()) {
                    case "Kick":
                        mSelectedKick = i;
                        mSelectedKickType = mInstrumentTypeSelector.getSelectedItem().toString();
                        break;
                    case "Clap":
                        mSelectedClap = i;
                        mSelectedClapType = mInstrumentTypeSelector.getSelectedItem().toString();
                        break;
                    case "Highhat":
                        mSelectedHat = i;
                        mSelectedHatType = mInstrumentTypeSelector.getSelectedItem().toString();
                        break;
                    case "Custom":
                        mSelectedCustom = i;
                        mSelectedCustomType = mInstrumentTypeSelector.getSelectedItem().toString();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { /* Nothing */}
        });

        // set up the on change listener for the seekbar
        mBPMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // range from 90 to 180
                int value = i + 90;
                mBPMTextView.setText("BPM: " + value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mNewCustomButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start the record intent
                Intent recordIntent = new Intent(MachineActivity.this, RecordActivity.class);
                MachineActivity.this.startActivity(recordIntent);
            }
        });

        // set up for audio playback
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSoundPool = new SoundPool.Builder().setMaxStreams(4).build();
    }

    // helper method for onCreate to find all the togglebuttons
    private void findAllBeatToggleButtons() {
        beat1 = (ToggleButton) findViewById(R.id.beat1Toggle);
        beat2 = (ToggleButton) findViewById(R.id.beat2Toggle);
        beat3 = (ToggleButton) findViewById(R.id.beat3Toggle);
        beat4 = (ToggleButton) findViewById(R.id.beat4Toggle);
        beat5 = (ToggleButton) findViewById(R.id.beat5Toggle);
        beat6 = (ToggleButton) findViewById(R.id.beat6Toggle);
        beat7 = (ToggleButton) findViewById(R.id.beat7Toggle);
        beat8 = (ToggleButton) findViewById(R.id.beat8Toggle);
        beat9 = (ToggleButton) findViewById(R.id.beat9Toggle);
        beat10 = (ToggleButton) findViewById(R.id.beat10Toggle);
        beat11 = (ToggleButton) findViewById(R.id.beat11Toggle);
        beat12 = (ToggleButton) findViewById(R.id.beat12Toggle);
        beat13 = (ToggleButton) findViewById(R.id.beat13Toggle);
        beat14 = (ToggleButton) findViewById(R.id.beat14Toggle);
        beat15 = (ToggleButton) findViewById(R.id.beat15Toggle);
        beat16 = (ToggleButton) findViewById(R.id.beat16Toggle);
    }

    // this method is called when changing instrument types to save the places that
    //beats were placed.
    private boolean[] SaveToggleLine() {
        boolean[] returnLine = {
                beat1.isChecked(),
                beat2.isChecked(),
                beat3.isChecked(),
                beat4.isChecked(),
                beat5.isChecked(),
                beat6.isChecked(),
                beat7.isChecked(),
                beat8.isChecked(),
                beat9.isChecked(),
                beat10.isChecked(),
                beat11.isChecked(),
                beat12.isChecked(),
                beat13.isChecked(),
                beat14.isChecked(),
                beat15.isChecked(),
                beat16.isChecked()
        };
        return returnLine;
    }

    // this method takes a boolean array and sets up the toggle buttons on the
    //beat grid to whatever was inside the array
    private void SetToggleLine(boolean[] inputLine) {
        beat1.setChecked(inputLine[0]);
        beat2.setChecked(inputLine[1]);
        beat3.setChecked(inputLine[2]);
        beat4.setChecked(inputLine[3]);
        beat5.setChecked(inputLine[4]);
        beat6.setChecked(inputLine[5]);
        beat7.setChecked(inputLine[6]);
        beat8.setChecked(inputLine[7]);
        beat9.setChecked(inputLine[8]);
        beat10.setChecked(inputLine[9]);
        beat11.setChecked(inputLine[10]);
        beat12.setChecked(inputLine[11]);
        beat13.setChecked(inputLine[12]);
        beat14.setChecked(inputLine[13]);
        beat15.setChecked(inputLine[14]);
        beat16.setChecked(inputLine[15]);
    }

    private void SetInstrumentType(int position){
        // if unset
        if (position != 0){

        }
    }

    public void updateSectionNumber(int i) {
        // section has changed, update it
        if (i != movingSectTime){
            movingSectTime = i;
            int onColor = Color.parseColor("#FF5FAE5D");
            int base = Color.parseColor("#FF000000");

            TriggerInstrumentsOnBeat(movingSectTime);
            switch (movingSectTime){
                case 1:
                    beat16.setTextColor(base);
                    beat1.setTextColor(onColor);
                    break;
                case 2:
                    beat1.setTextColor(base);
                    beat2.setTextColor(onColor);
                    break;
                case 3:
                    beat2.setTextColor(base);
                    beat3.setTextColor(onColor);
                    break;
                case 4:
                    beat3.setTextColor(base);
                    beat4.setTextColor(onColor);
                    break;
                case 5:
                    beat4.setTextColor(base);
                    beat5.setTextColor(onColor);
                    break;
                case 6:
                    beat5.setTextColor(base);
                    beat6.setTextColor(onColor);
                    break;
                case 7:
                    beat6.setTextColor(base);
                    beat7.setTextColor(onColor);
                    break;
                case 8:
                    beat7.setTextColor(base);
                    beat8.setTextColor(onColor);
                    break;
                case 9:
                    beat8.setTextColor(base);
                    beat9.setTextColor(onColor);
                    break;
                case 10:
                    beat9.setTextColor(base);
                    beat10.setTextColor(onColor);
                    break;
                case 11:
                    beat10.setTextColor(base);
                    beat11.setTextColor(onColor);
                    break;
                case 12:
                    beat11.setTextColor(base);
                    beat12.setTextColor(onColor);
                    break;
                case 13:
                    beat12.setTextColor(base);
                    beat13.setTextColor(onColor);
                    break;
                case 14:
                    beat13.setTextColor(base);
                    beat14.setTextColor(onColor);
                    break;
                case 15:
                    beat14.setTextColor(base);
                    beat15.setTextColor(onColor);
                    break;
                case 16:
                    beat15.setTextColor(base);
                    beat16.setTextColor(onColor);
                    break;
            }
        }
    }

    private void TriggerInstrumentsOnBeat(int beat){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float v = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        beat--; // adjust for array
        if (kickLine[beat] == true){
             Log.i("SOUND!", "kick on beat" + beat);
            mSoundPool.play(kickSoundId, v, v, 1, 0, 1f);
        }
        if (clapLine[beat] == true){
            Log.i("SOUND!", "clap on beat" + beat);
            mSoundPool.play(clapSoundId, v, v, 1, 0, 1f);
        }
        if (cymbalLine[beat] == true){
            Log.i("SOUND!", "hat on beat" + beat);
            mSoundPool.play(hhatSoundId, v, v, 1, 0, 1f);
        }
        //TODO: add custom sound
    }

    private void LoadSounds_AwaitCompletion(){
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                Log.i("SOUNDLOADED", "loaded sound with ID:" + i);
            }
        });

        String kickName = "k_"+ mSelectedKickType.toLowerCase();
        kickSoundId = mSoundPool.load(this,
                MachineActivity.this.getResources().getIdentifier(kickName, "raw", MachineActivity.this.getPackageName()),
                1);
        String clapName = "c_"+ mSelectedClapType.toLowerCase();
        clapSoundId = mSoundPool.load(this,
                MachineActivity.this.getResources().getIdentifier(clapName, "raw", MachineActivity.this.getPackageName()),
                1);
        String hhatName = "h_"+ mSelectedHatType.toLowerCase();
        hhatSoundId = mSoundPool.load(this,
                MachineActivity.this.getResources().getIdentifier(hhatName, "raw", MachineActivity.this.getPackageName()),
                1);
        //TODO: load the custom sound to the SoundPool
    }
}
