/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.oboe.hellooboe;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.sample.audio_device.AudioDeviceListEntry;
import com.google.sample.audio_device.AudioDeviceSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    private static final long UPDATE_LATENCY_EVERY_MILLIS = 1000;
    private static final Integer[] CHANNEL_COUNT_OPTIONS = {1, 2, 3, 4, 5, 6, 7, 8};
    // Default to 4-channel (OPTIONS is zero-based array so index 3 = 4 channels)
    private static final int CHANNEL_COUNT_DEFAULT_OPTION_INDEX = 7;
    private static final int[] BUFFER_SIZE_OPTIONS = {0, 1, 2, 4, 8};
    private static final String[] AUDIO_API_OPTIONS = {"Unspecified", "OpenSL ES", "AAudio"};

    private Spinner mAudioApiSpinner;
    private AudioDeviceSpinner mPlaybackDeviceSpinner;
    private Spinner mChannelCountSpinner;
    private Spinner mBufferSizeSpinner;
    private TextView mLatencyText;
    private Timer mLatencyUpdater;

    private Button mButtonChannel1, mButtonChannel2, mButtonChannel3, mButtonChannel4,
        mButtonChannel5, mButtonChannel6, mButtonChannel7, mButtonChannel8;

    /*
     * Hook to user control to start / stop audio playback:
     *    touch-down: start, and keeps on playing
     *    touch-up: stop.
     * simply pass the events to native side.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                PlaybackEngine.setToneOn(true);
                break;
            case (MotionEvent.ACTION_UP):
                PlaybackEngine.setToneOn(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAudioApiSpinner();
        setupPlaybackDeviceSpinner();
        setupChannelCountSpinner();
        setupBufferSizeSpinner();
        setupChannelButtons();

        // initialize native audio system
        PlaybackEngine.create();

        // Periodically update the UI with the output stream latency
        mLatencyText = findViewById(R.id.latencyText);
        setupLatencyUpdater();
    }

    private void setupChannelCountSpinner() {
        mChannelCountSpinner = findViewById(R.id.channelCountSpinner);

        ArrayAdapter<Integer> channelCountAdapter = new ArrayAdapter<Integer>(this, R.layout.channel_counts_spinner, CHANNEL_COUNT_OPTIONS);
        mChannelCountSpinner.setAdapter(channelCountAdapter);
        mChannelCountSpinner.setSelection(CHANNEL_COUNT_DEFAULT_OPTION_INDEX);

        mChannelCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setChannelCount(CHANNEL_COUNT_OPTIONS[mChannelCountSpinner.getSelectedItemPosition()]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupBufferSizeSpinner() {
        mBufferSizeSpinner = findViewById(R.id.bufferSizeSpinner);
        mBufferSizeSpinner.setAdapter(new SimpleAdapter(
                this,
                createBufferSizeOptionsList(), // list of buffer size options
                R.layout.buffer_sizes_spinner, // the xml layout
                new String[]{getString(R.string.buffer_size_description_key)}, // field to display
                new int[]{R.id.bufferSizeOption} // View to show field in
        ));

        mBufferSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setBufferSizeInBursts(getBufferSizeInBursts());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupPlaybackDeviceSpinner() {
        mPlaybackDeviceSpinner = findViewById(R.id.playbackDevicesSpinner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlaybackDeviceSpinner.setDirectionType(AudioManager.GET_DEVICES_OUTPUTS);
            mPlaybackDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PlaybackEngine.setAudioDeviceId(getPlaybackDeviceId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    private void setupAudioApiSpinner() {
        mAudioApiSpinner = findViewById(R.id.audioApiSpinner);
        mAudioApiSpinner.setAdapter(new SimpleAdapter(
                this,
                createAudioApisOptionsList(),
                R.layout.audio_apis_spinner, // the xml layout
                new String[]{getString(R.string.audio_api_description_key)}, // field to display
                new int[]{R.id.audioApiOption} // View to show field in
        ));

        mAudioApiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setAudioApi(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupChannelButtons() {
        mButtonChannel1 = findViewById(R.id.button_channel1);
        mButtonChannel2 = findViewById(R.id.button_channel2);
        mButtonChannel3 = findViewById(R.id.button_channel3);
        mButtonChannel4 = findViewById(R.id.button_channel4);
        mButtonChannel5 = findViewById(R.id.button_channel5);
        mButtonChannel6 = findViewById(R.id.button_channel6);
        mButtonChannel7 = findViewById(R.id.button_channel7);
        mButtonChannel8 = findViewById(R.id.button_channel8);

        mButtonChannel1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn1(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn1(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn2(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn2(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn3(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn3(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn4(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn4(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn5(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn5(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn6(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn6(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel7.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn7(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn7(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

        mButtonChannel8.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PlaybackEngine.setChannelOn8(true);
                        PlaybackEngine.setToneOn(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        PlaybackEngine.setChannelOn8(false);
                        PlaybackEngine.setToneOn(false);
                        break;
                }
                return false;
            }
        });

    }

    private int getPlaybackDeviceId() {
        return ((AudioDeviceListEntry) mPlaybackDeviceSpinner.getSelectedItem()).getId();
    }

    private int getBufferSizeInBursts() {
        @SuppressWarnings("unchecked")
        HashMap<String, String> selectedOption = (HashMap<String, String>)
                mBufferSizeSpinner.getSelectedItem();

        String valueKey = getString(R.string.buffer_size_value_key);

        // parseInt will throw a NumberFormatException if the string doesn't contain a valid integer
        // representation. We don't need to worry about this because the values are derived from
        // the BUFFER_SIZE_OPTIONS int array.
        return Integer.parseInt(selectedOption.get(valueKey));
    }

    private void setupLatencyUpdater() {

        //Update the latency every 1s
        TimerTask latencyUpdateTask = new TimerTask() {
            @Override
            public void run() {

                final String latencyStr;

                if (PlaybackEngine.isLatencyDetectionSupported()) {

                    double latency = PlaybackEngine.getCurrentOutputLatencyMillis();
                    if (latency >= 0) {
                        latencyStr = String.format(Locale.getDefault(), "%.2fms", latency);
                    } else {
                        latencyStr = "Unknown";
                    }
                } else {
                    latencyStr = getString(R.string.only_supported_on_api_26);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLatencyText.setText(getString(R.string.latency, latencyStr));
                    }
                });
            }
        };

        mLatencyUpdater = new Timer();
        mLatencyUpdater.schedule(latencyUpdateTask, 0, UPDATE_LATENCY_EVERY_MILLIS);

    }

    @Override
    protected void onDestroy() {
        if (mLatencyUpdater != null) mLatencyUpdater.cancel();
        PlaybackEngine.delete();
        super.onDestroy();
    }

    /**
     * Creates a list of buffer size options which can be used to populate a SimpleAdapter.
     * Each option has a description and a value. The description is always equal to the value,
     * except when the value is zero as this indicates that the buffer size should be set
     * automatically by the audio engine
     *
     * @return list of buffer size options
     */
    private List<HashMap<String, String>> createBufferSizeOptionsList() {

        ArrayList<HashMap<String, String>> bufferSizeOptions = new ArrayList<>();

        for (int i : BUFFER_SIZE_OPTIONS) {
            HashMap<String, String> option = new HashMap<>();
            String strValue = String.valueOf(i);
            String description = (i == 0) ? getString(R.string.automatic) : strValue;
            option.put(getString(R.string.buffer_size_description_key), description);
            option.put(getString(R.string.buffer_size_value_key), strValue);

            bufferSizeOptions.add(option);
        }

        return bufferSizeOptions;
    }

    private List<HashMap<String, String>> createAudioApisOptionsList() {

        ArrayList<HashMap<String, String>> audioApiOptions = new ArrayList<>();

        for (int i = 0; i < AUDIO_API_OPTIONS.length; i++) {
            HashMap<String, String> option = new HashMap<>();
            option.put(getString(R.string.buffer_size_description_key), AUDIO_API_OPTIONS[i]);
            option.put(getString(R.string.buffer_size_value_key), String.valueOf(i));
            audioApiOptions.add(option);
        }
        return audioApiOptions;
    }
}
