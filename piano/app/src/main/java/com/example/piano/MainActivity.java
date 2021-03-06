package com.example.piano;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
  private static String TAG = "AudioClient";

  // the server information
  private static final String SERVER = "192.168.0.107";
  private static final int PORT = 50005;

  // the audio recording options
  public static final int RECORDING_RATE = 44100;
  private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

  // the button the user presses to send the audio stream to the server
  private Button sendAudioButton;

  // the audio recorder
  private AudioRecord recorder;

  // the minimum buffer size needed for audio recording
  public static int BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL, FORMAT);

  // are we currently sending audio data
  private boolean currentlySendingAudio = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Log.i(TAG, "Creating the Audio Client with minimum buffer of " + BUFFER_SIZE + " bytes");

    // set up the button
    sendAudioButton = (Button) findViewById(R.id.btnStart);
    sendAudioButton.setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

          case MotionEvent.ACTION_DOWN:
            startStreamingAudio();
            break;

          case MotionEvent.ACTION_UP:
            stopStreamingAudio();
            break;
        }

        return false;
      }
    });
  }

  private void startStreamingAudio() {

    Log.i(TAG, "Starting the audio stream");
    currentlySendingAudio = true;
    startStreaming();
  }

  private void stopStreamingAudio() {

    Log.i(TAG, "Stopping the audio stream");
    currentlySendingAudio = false;
    recorder.release();
  }

  private static final short SHORT_DIVISOR = (short) (-1 * Short.MIN_VALUE);

  public void convert(final short[] array, final float[] convertedArray) {
    int arrayLength = array.length;
    int convertedArrayLength = convertedArray.length;

    for (int i = 0; i < arrayLength && i < convertedArrayLength; i++) {
      convertedArray[i] = ((float) array[i]) / SHORT_DIVISOR;
      convertedArray[i] = convertedArray[i] < -1 ? -1 : Math.min(convertedArray[i], 1);
    }
  }

  private void startStreaming() {
    System.out.println("start streaming");
    Log.i(TAG, "Starting the background thread to stream the audio data");

    Thread streamThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Log.d(TAG, "Creating the buffer of size " + BUFFER_SIZE);
          short[] buffer = new short[BUFFER_SIZE / 2];

          Log.d(TAG, "Creating the AudioRecord");
          recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE * 10);

          Log.d(TAG, "AudioRecord recording...");
          recorder.startRecording();

          List<float[]> l = new ArrayList<>();

          while (currentlySendingAudio == true) {
            // read the data into the buffer
            int read = recorder.read(buffer, 0, buffer.length);
//            Log.d(TAG, "read " + read);
            float[] floatBuffer = new float[buffer.length];
            convert(buffer, floatBuffer);
            double key = new YINPitchDetector().detect(floatBuffer);
            Log.d(TAG, System.currentTimeMillis() + " key=" + key);
            TextView info = findViewById((R.id.lblInfo));
            info.setText("k=" + key);

            l.add(floatBuffer);
          }

          for (int i = 0; i < l.size(); i++) {
            float[] ll = l.get(i);
            String msg = "buf " + i + " of " + l.size() + " length=" + ll.length;
            StringBuilder sb = new StringBuilder();
            sb.append(msg + "\n");
            for (int j = 0; j < ll.length; j++) {
              sb.append(ll[j] + "\n");
            }
            byte[] send = sb.toString().getBytes();
            DatagramSocket socket = new DatagramSocket();
            final InetAddress serverAddress = InetAddress.getByName(SERVER);
            DatagramPacket packet = new DatagramPacket(send, send.length,                  serverAddress, PORT);
            socket.send(packet);
            socket.close();
            Log.d(TAG, "send " + msg);
          }

          Log.d(TAG, "AudioRecord finished recording");
        } catch (Exception e) {
          Log.e(TAG, "Exception: " + e);
        }
      }
    });

    // start the thread
    streamThread.start();
  }
}