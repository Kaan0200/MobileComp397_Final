package com.example.kaan.vocomaster3000;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.reflect.Array;

public class MachineActivity extends AppCompatActivity {

    // fill the ListViews with String Arrays
    public Spinner mInstrumentSelector;
    public Spinner mInstrumentTypeSelector;
    public Spinner mParameterSelector;
    public SeekBar mBPMSeekBar;
    public TextView mBPMTextView;
    public ToggleButton mPlayToggle;
    public Button mNewCustomButton;

    public int mSelectedKick;
    public int mSelectedCymbal;
    public int mSelectedClap;
    public int mSelectedCustom;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);

        // get the custom button
        mNewCustomButton = (Button) findViewById(R.id.newCustomButton);
        // get the spinners
        mInstrumentSelector = (Spinner) findViewById(R.id.instrumentSelectorSpinner);
        mInstrumentTypeSelector = (Spinner) findViewById(R.id.instrumentTypeSpinner);
        mParameterSelector = (Spinner) findViewById(R.id.parameterSpinner);
        // get the slider, set initial value
        mBPMSeekBar = (SeekBar) findViewById(R.id.bpmSeekbar);
        mBPMSeekBar.setProgress(38);
        // get thmBPMTextViewview, set text
        mBPMTextView = (TextView) findViewById(R.id.bpmText);
        mBPMTextView.setText("BMP: 128");
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
        mParameterSelector.setAdapter(parameterAdapter);
        // create adapters for the instrument types
        final ArrayAdapter<String> kickAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.kick_type));
        final ArrayAdapter<String> clapAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.clap_type));
        final ArrayAdapter<String> cymbAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.cymbal_type));

        // set up the on change listener for the play button
        mPlayToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == false) { // STOP
                    //TODO: Get stop to stop
                } else { // PLAY
                    //TODO: Get play working
                }
            }
        });

        // set up the on change listener for the instrument spinner
        mInstrumentSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 1.
                // Save the last Line into its intArray, and the selected type
                switch (lastLine) {
                    case "Kick":
                        kickLine = GetToggleLine();
                        mSelectedKick = mInstrumentTypeSelector.getSelectedItemPosition();
                        break;
                    case "Clap":
                        clapLine = GetToggleLine();
                        mSelectedClap = mInstrumentTypeSelector.getSelectedItemPosition();
                        break;
                    case "Cymbal":
                        cymbalLine = GetToggleLine();
                        mSelectedCymbal = mInstrumentTypeSelector.getSelectedItemPosition();
                        break;
                    case "Custom":
                        customLine = GetToggleLine();
                        mSelectedCustom = mInstrumentTypeSelector.getSelectedItemPosition();
                        break;
                }
                // 2.
                // clean line and set into new values
                lastLine = (String)mInstrumentSelector.getSelectedItem();

                // 3.
                // last line is now current, so load in the appropriate intArray
                switch (lastLine){
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
                    case "Cymbal":
                        SetToggleLine(cymbalLine);
                        mInstrumentTypeSelector.setAdapter(cymbAdapter);
                        break;
                    case "Custom":
                        SetToggleLine(customLine);
                        //TODO: create and handle adapter for custom sounds
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { /*Nothing*/ }
        });

        // set up the on change listener for the seekbar
        mBPMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // range from 90 to 180
                int value = i + 90;
                mBPMTextView.setText("BPM: "+ value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mNewCustomButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                //TODO: Start a new activity and record sound there
            }
        });
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
    private boolean[] GetToggleLine() {
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

    private void PlaySequencer(){

    }
}
