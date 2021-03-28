package com.example.datastoreapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_LOCATION=1;
    EditText editTextEmf;
    EditText editTextlat;
    Button start;
    Button stop;
    LocationManager locationManager;
    String latitude,longitude;
    private static SensorManager sensorManager;
    private Sensor sensor;

    DatabaseReference databaseArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);



        editTextEmf = (EditText) findViewById(R.id.editTextEmf);
        start=(Button) findViewById(R.id.start);
        stop=(Button) findViewById(R.id.stop);
        editTextlat = findViewById(R.id.editTextlat);
        databaseArtists= FirebaseDatabase.getInstance().getReference("artists");




        // Create the Handler object (on the main thread by default)

// Define the code block to be executed


// Start the initial runnable task by posting through the handler



        Handler handler = new Handler();
        emfRefresh();





        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startRepeatingTask();
                locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    onGPS();
                }
                else{
                    getLocation();
                }



            }

        });




        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopRepeatingTask();


            }
        });






        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
                     ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);

        }
        else{
            Location LocationGps=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Location LocationPassive =locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(LocationGps != null)
            {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
                editTextlat.setText("Lat= "+latitude+"\n"+"long = "+longitude);

            }
//            else if(LocationNetwork != null){
//                double lat = LocationNetwork.getLatitude();
//                double longi = LocationNetwork.getLongitude();
//                latitude = String.valueOf(lat);
//                latitude = String.valueOf(longi);
//                editTextlat.setText("Lat= "+latitude+"\n"+"long = "+longitude);
//
//            }
//            else if(LocationPassive != null){
//                double lat = LocationPassive.getLatitude();
//                double longi = LocationPassive.getLongitude();
//                latitude = String.valueOf(lat);
//                latitude = String.valueOf(longi);
//                editTextlat.setText("Lat= "+latitude+"\n"+"long = "+longitude);
//
//            }
            else {
                Toast.makeText(this,"Can't get your Location",Toast.LENGTH_LONG).show();
            }

        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));

            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }

    Handler handler = new Handler();


    public void emfRefresh(){

        startRepeatingTask();
        stopRepeatingTask();
    }

    public Runnable emfUpdate = new Runnable() {

        @Override
        public void run() {
            addArtist();
            // Repeat this the same runnable code block again another 2 seconds
            // 'this' is referencing the Runnable object
            handler.postDelayed(this, 10000);

        }
    };

    void startRepeatingTask()
    {
        emfUpdate.run();
    }

    void stopRepeatingTask()
    {
        handler.removeCallbacks(emfUpdate);
    }



    @Override
    protected void onResume(){
        super.onResume();
        if(sensor != null){
            sensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this,"NOT supported !", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
    @Override

    protected void onPause(){

        super.onPause();

        sensorManager.unregisterListener(this);

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent){

        float azimuth = Math.round(sensorEvent.values[0]);
        float pitch = Math.round(sensorEvent.values[1]);
        float roll = Math.round(sensorEvent.values[2]);
        double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
        String text = String.format("%.0f",tesla);
        editTextEmf.setText(text + " μT");

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i){

    }

    private void addArtist(){
        String value = editTextEmf.getText().toString().trim();
        if(!TextUtils.isEmpty(value)){
            String id = databaseArtists.push().getKey();

            Artist artist = new Artist(id,value);

            databaseArtists.child(id).setValue(artist);
            Toast.makeText(this,"Data Added", Toast.LENGTH_LONG).show();

        }
        else{
            Toast.makeText(this,"Welcome To Emf App", Toast.LENGTH_LONG).show();
        }

    }





}
