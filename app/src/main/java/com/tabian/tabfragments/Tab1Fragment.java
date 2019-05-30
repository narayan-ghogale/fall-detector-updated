package com.tabian.tabfragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Tab1Fragment extends Fragment implements SensorEventListener {
    private static final String TAG = "Tab1Fragment";

    static float deltaX = 0, deltaY = 0, deltaZ = 0;
    static float xAccel = 0, yAccel = 0, zAccel = 0;
    static float xPreviousAccel = 0, yPreviousAccel = 0, zPreviousAccel = 0;

    static float vibrateThreshold = 20;
    static float angleThreshold = 25;
    static float accels[], mags[];

    static SensorManager sensorManager;
    static Sensor accelerometer;
    static LocationManager locationManager;
    static LocationListener locationListener;

    static double latitude, longitude;

    static ArrayList<String> callLogs;

    TextView currentX, currentY, currentZ, angleX, angleY, angleZ, maxX, maxY, maxZ;

    static public Vibrator v;

    static boolean moved, accel_start = true;
    static int i;

    static TextView tView;
    //Declare a variable to hold count down timer's paused status
    private boolean isPaused = false;
    //Declare a variable to hold count down timer's paused status
    private static boolean isCanceled = false;

    //Declare a variable to hold CountDownTimer remaining time
    private long timeRemaining = 0, millisInFuture, countDownInterval;
    static Button btnCancel, button;
    ProgressBar progressBarCircle;

    private String sms = "";
    View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab1_fragment, container, false);

        initializeViews();

        v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        //Intent i = new Intent(this, SensorService.class);
        //startService(i);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            //vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fail! we dont have an accelerometer!
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                maxY.setText(String.valueOf(latitude));
                maxZ.setText(String.valueOf(longitude));
                sms = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                //Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //startActivity(i);
            }
        };

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }



        //Timer
        btnCancel = (Button) view.findViewById(R.id.button);
        tView = (TextView) view.findViewById(R.id.tv);
        isCanceled = false;
        millisInFuture = 15000; //25 seconds
        countDownInterval = 1000; //1 second
        btnCancel.setEnabled(true);

        CountDownTimer timer;

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.VIBRATE}
                        , 10);
            }
            return;
        }
        // this code won'textView execute IF permissions are not allowed, because in the line above there is return statement.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission


            }
        });


    }


    public void initializeViews() {
        currentX = (TextView) view.findViewById(R.id.currentX);
        currentY = (TextView) view.findViewById(R.id.currentY);
        currentZ = (TextView) view.findViewById(R.id.currentZ);

        maxX = (TextView) view.findViewById(R.id.maxX);
        maxY = (TextView) view.findViewById(R.id.maxY);
        maxZ = (TextView) view.findViewById(R.id.maxZ);
    }

    //onResume() register the accelerometer for listening the events
    /*protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }*/


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //Toast.makeText(getApplicationContext(), String.valueOf(xAccel), Toast.LENGTH_LONG).show();
        displayCleanValues();
        displayCurrentValues();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accels = event.values;
            updateAccelParameters(event.values[0], event.values[1], event.values[2]);
        }

        checkFall();
    }


    public void checkFall() {
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ < 2)
            deltaZ = 0;
        if (deltaZ > vibrateThreshold) {
            moved = false;
            i = 5;

            isCanceled = false;
            isPaused = false;
            moved = detect_motion();
            v.vibrate(1000);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            //getCallLogs();
            startTimer();
            maxX.setText("Fall Detected !");
        }
    }


    private void startTimer()
    {
        btnCancel.setVisibility(View.VISIBLE);
        //Initialize a new CountDownTimer instance
        new CountDownTimer(millisInFuture,countDownInterval){
            public void onTick(long millisUntilFinished){
                if(isPaused || isCanceled)
                {
                    //If the user request to cancel or paused the
                    //CountDownTimer we will cancel the current instance
                    cancel();
                }
                else {
                    //Display the remaining seconds to app interface
                    //1 second = 1000 milliseconds
                    tView.setText("Counter :\n  " + millisUntilFinished / 1000 + " s");
                    //Put count down timer remaining time in a variable
                    timeRemaining = millisUntilFinished;
                }
            }

            public void onFinish(){
                //Do something when count down finished
                tView.setText("Time over...");
                onSend();
                openWhatsApp();
                v.vibrate(1000);
                btnCancel.setVisibility(View.GONE);
            }

        }.start();
    }


    public boolean detect_motion()
    {
        return ((deltaX>2 && deltaY>2) ||
                (deltaY>2 && deltaZ>2) ||
                (deltaX>2 && deltaZ>2));
    }


    private void updateAccelParameters(float xNewAccel, float yNewAccel, float zNewAccel)
    {
        if(accel_start)
        {
            xPreviousAccel = xNewAccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccel;
            accel_start = false;
        }
        else
        {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }

        xAccel = xNewAccel;
        yAccel = yNewAccel;
        zAccel = zNewAccel;

        deltaX = Math.abs(xAccel - xPreviousAccel);
        deltaY = Math.abs(yAccel - yPreviousAccel);
        deltaZ = Math.abs(zAccel - zPreviousAccel);
    }


    public void displayCleanValues()
    {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }


    public void displayCurrentValues()
    {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    public void onSend()
    {
        SmsManager smsManager = SmsManager.getDefault();
        for(String phoneNumber: Tab2Fragment.x)
        {
            String smsMessage = "Help";

            if (phoneNumber == null || phoneNumber.length() == 0 || smsMessage.length() == 0 || smsMessage == null) {
                continue;
            }
            smsManager.sendTextMessage(phoneNumber,null, sms,null,null);
        }
        Toast.makeText(getActivity(),"SMS Sent."
                ,Toast.LENGTH_SHORT).show();
    }

    public void openWhatsApp()
    {
        for(String toNumber: Tab2Fragment.x)
        {
            try {
                //String text = "This is a test";// Replace with your message.

                String number = "91"+toNumber;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                String s = "Location Coordinates : " + latitude + "," + longitude;
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+number +"&text="+s));
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(getActivity(),permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public static void stopTimer() {
        isCanceled = true;

        tView.setText("CountDownTimer Canceled/stopped.");
        btnCancel.setVisibility(View.GONE);
    }
}