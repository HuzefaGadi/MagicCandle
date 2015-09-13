package com.huzefagadi.magiccandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends Activity {

    ImageView candle, flame;
    List<Integer> listOfImages;
    List<Integer> listOfImagesForSmoke;
    boolean flameIsRunning, smokeIsRunning;
    int countForFLames = 0, countForSmoke = 0;
    private static final int sampleRate = 8000;
    private AudioRecord audio;
    private int bufferSize;
    private double lastLevel = 0;
    private Thread thread;
    private static final int SAMPLE_DELAY = 75;
    CountDownTimer timerForFlame, timerForSmoke;
    boolean isFlashOn = false;
    private Camera camera;
    private Camera.Parameters params;
    private SurfaceTexture mPreviewTexture;
    Thread threadForFlashStart, threadForFlashStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listOfImages = new ArrayList<Integer>();
        listOfImagesForSmoke = new ArrayList<Integer>();
        flameIsRunning = false;
        smokeIsRunning = false;

        listOfImages.add(R.drawable.candle_fire1);
        listOfImages.add(R.drawable.candle_fire2);
        listOfImages.add(R.drawable.candle_fire3);
        listOfImages.add(R.drawable.candle_fire4);
        listOfImages.add(R.drawable.candle_fire5);
        listOfImages.add(R.drawable.candle_fire6);

        listOfImagesForSmoke.add(R.drawable.smoke_1);
        listOfImagesForSmoke.add(R.drawable.smoke_2);
        listOfImagesForSmoke.add(R.drawable.smoke_3);
        listOfImagesForSmoke.add(R.drawable.smoke_4);
        listOfImagesForSmoke.add(R.drawable.smoke_5);
        listOfImagesForSmoke.add(R.drawable.smoke_6);
        listOfImagesForSmoke.add(R.drawable.smoke_7);
        listOfImagesForSmoke.add(R.drawable.smoke_8);
        listOfImagesForSmoke.add(R.drawable.smoke_9);
        listOfImagesForSmoke.add(R.drawable.smoke_10);
        listOfImagesForSmoke.add(R.drawable.smoke_11);
        listOfImagesForSmoke.add(R.drawable.smoke_12);
        listOfImagesForSmoke.add(R.drawable.smoke_13);
        listOfImagesForSmoke.add(R.drawable.smoke_14);
        listOfImagesForSmoke.add(R.drawable.smoke_15);
        listOfImagesForSmoke.add(R.drawable.smoke_16);
        listOfImagesForSmoke.add(R.drawable.smoke_17);
        listOfImagesForSmoke.add(R.drawable.smoke_18);
        listOfImagesForSmoke.add(R.drawable.smoke_19);
        listOfImagesForSmoke.add(R.drawable.smoke_20);
        listOfImagesForSmoke.add(R.drawable.smoke_21);
        listOfImagesForSmoke.add(R.drawable.smoke_22);


        candle = (ImageView) findViewById(R.id.imageView1);
        flame = (ImageView) findViewById(R.id.imageView2);
        getCamera();

        timerForFlame = new CountDownTimer(30000, 100) {

            @Override
            public void onTick(long arg0) {
                // TODO Auto-generated method stub
                if (countForFLames == listOfImages.size() - 1) {
                    flame.setImageResource(listOfImages.get(countForFLames));
                    countForFLames = 0;

                } else {
                    flame.setImageResource(listOfImages.get(countForFLames));
                    countForFLames++;
                }

            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                timerForFlame.start();

            }
        };

        timerForSmoke = new CountDownTimer(2300, 100) {

            @Override
            public void onTick(long arg0) {
                // TODO Auto-generated method stub
                if (countForSmoke == listOfImagesForSmoke.size()) {

                    timerForSmoke.onFinish();
                    //timerForSmoke.cancel();

                } else {
                    flame.setImageResource(listOfImagesForSmoke.get(countForSmoke));
                    countForSmoke++;

                }

            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                flame.setImageBitmap(null);
                countForSmoke = 0;
                smokeIsRunning = false;
            }
        };


        candle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(flameIsRunning)
                {
                    startSmoke();
                }
                else
                {
                    startFlame();
                }

            }
        });


        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }


        new CountDownTimer(2000, 1000) {

            @Override
            public void onTick(long arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                candle.setImageResource(R.drawable.lampmain);
                startFlame();
            }
        }.start();
        //	startFlame();
    }

    protected void onResume() {
        super.onResume();
        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audio.startRecording();
        thread = new Thread(new Runnable() {
            public void run() {
                while (thread != null && !thread.isInterrupted()) {
                    //Let's make the thread sleep for a the approximate sampling time
                    try {
                        Thread.sleep(SAMPLE_DELAY);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    readAudioBuffer();//After this call we can get the last value assigned to the lastLevel variable

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (lastLevel > 400) {
                                System.out.println("HIGH PITCH");
                                startSmoke();
                            }
                        }
                    });
                }
            }
        });
        thread.start();


    }

    /**
     * Functionality that gets the sound level out of the sample
     */
    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult = 1;

            if (audio != null) {

                // Sense the voice...
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                double sumLevel = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
                }
                lastLevel = Math.abs((sumLevel / bufferReadResult));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerForFlame != null) {
            timerForFlame.cancel();
            flame.setImageBitmap(null);
            flameIsRunning = false;
        }
        thread.interrupt();
        thread = null;
        if (threadForFlashStart != null) {
            threadForFlashStart.interrupt();
            threadForFlashStart = null;

        }
        if (threadForFlashStop != null) {
            threadForFlashStop.interrupt();
            threadForFlashStop = null;

        }
        try {
            if (audio != null) {
                audio.stop();
                audio.release();
                audio = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startSmoke() {
        if (flameIsRunning) {

            turnOffFlash();
            if (timerForFlame != null) {
                timerForFlame.cancel();
                timerForSmoke.start();
                smokeIsRunning = true;
                flameIsRunning = false;
            }
        }
    }

    public void startFlame() {
        if (!flameIsRunning && !smokeIsRunning) {

            turnOnFlash();
            System.out.println("outside");
            if (timerForFlame != null) {
                System.out.println("inside");
                timerForFlame.cancel();
                timerForFlame.start();
                flameIsRunning = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();

            } catch (RuntimeException e) {
                //Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }

    /*
    * Turning On flash
    */
    private void turnOnFlash() {

        threadForFlashStart = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isFlashOn) {
                    if (camera == null || params == null) {

                        threadForFlashStart.interrupt();
                        threadForFlashStart = null;
                        return;
                    }
// play sound

                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    mPreviewTexture = new SurfaceTexture(0);
                    try {
                        camera.setPreviewTexture(mPreviewTexture);
                    } catch (IOException ex) {
                        // Ignore
                    }
                    camera.startPreview();
                    isFlashOn = true;
                    threadForFlashStart.interrupt();
                    threadForFlashStart = null;
// changing button/switch image

                }
            }
        });
        if(!threadForFlashStart.isAlive())
        threadForFlashStart.start();

    }

    private void turnOffFlash() {

        threadForFlashStop = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFlashOn) {
                    if (camera == null || params == null) {
                        threadForFlashStop.interrupt();
                        threadForFlashStop = null;
                        return;
                    }
                    // play sound
                    params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    camera.stopPreview();
                    isFlashOn = false;
                    threadForFlashStop.interrupt();
                    threadForFlashStop = null;
                }
            }
        });

        if(!threadForFlashStop.isAlive())
        threadForFlashStop.start();

    }
}
