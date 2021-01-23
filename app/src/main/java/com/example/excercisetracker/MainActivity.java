package com.example.excercisetracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.Button;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "My errors";
    private Button recordButton;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    boolean recordmode = false;
    TextView txtLat;
    String lat;
    String provider;
    Location mylastlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordButton = (Button) findViewById(R.id.RecordButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(!recordmode){
                    recordButton.setText("Stop Recording");
                    recordmode=true;
                    toastMessage("Recording Started!");
                    startrecordinglocation();

                }else{
                    recordButton.setText("Start Recording");
                    recordmode=false;
                    toastMessage("Recording Finished!");
                    Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                    startActivity(intent);

                }

            }
        });

    }
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mylastlocation=location;

    }
    public void startrecordinglocation(){

        // create folder in external storage if doesnt exist
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/GPStracks");
        if (!myDir.exists()) {
            try {
                if (myDir.mkdirs()) {
                    toastMessage("Folder Created");
                } else {
                    toastMessage("Unable to create Folder");
                }
            }catch(Exception e){
                toastMessage(e.getMessage());
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String filename= df.format(new Date())+".gpx";
        File file = new File (myDir, filename);

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(getfileheaders(filename));
            writeCoordinatestoFile(writer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            toastMessage("Error in file writing");
            Log.e(TAG, "Error Writting Path",e);
           
        }


    }
    public String getfileheaders(String filename){
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + filename + "</name><trkseg>\n";
    return header+name;
    }
    public String getfilefooter(){
        String footer = "</trkseg></trk></gpx>";
    return footer;
    }
    public void writeCoordinatestoFile(FileWriter writer){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(mylastlocation!=null){
                    if(recordmode==true) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                toastMessage("Lat: " + mylastlocation.getLatitude() + " , Lon : " + mylastlocation.getLongitude() + " Alt: " + mylastlocation.getAltitude());
                               Location l=mylastlocation;
                                String segment= "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\"><time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
                                try {
                                    writer.append(segment);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }else{
                        this.cancel();
                        try {
                            writer.append(getfilefooter());
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                }
            }
        }, 0, 5000);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        toastMessage("Latitude, status");
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        toastMessage("Latitude, enable");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        toastMessage("Latitude, disable");
    }
}