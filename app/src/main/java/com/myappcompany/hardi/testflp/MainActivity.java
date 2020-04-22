package com.myappcompany.hardi.testflp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
       GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient client;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    public static final int REQUEST_CODE_FINE_LOCATION_PERMISSION = 1;
    public static final long UPDATE_INTERVAL = 5000;
    public static final long FASTEST_INTERVAL = 5000;

    private TextView locationTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.location_text_view);
        progressBar = findViewById(R.id.progress_bar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_FINE_LOCATION_PERMISSION
            );
        } else {
            startLocationUpdates();
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (client != null){
            client.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null && client.isConnected()){
            LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(new LocationCallback());
            client.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_FINE_LOCATION_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationUpdates();
            } else {
                Toast.makeText(this, "You need to add permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPlayServices(){
        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);
        if( errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(MainActivity.this, "No Services", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            errorDialog.show();
        }
    }

    private void startLocationUpdates(){
        progressBar.setVisibility(View.VISIBLE);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if(locationResult != null){
                            progressBar.setVisibility(View.GONE);
                            Location location = locationResult.getLastLocation();
                            locationTextView.setText(String.format("Lat: %s Lon: %s", location.getLatitude(), location.getLongitude()));
                        }
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);
                    }
                }, null);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            progressBar.setVisibility(View.GONE);
                            locationTextView.setText(String.format("Lat: %s Lon: %s", location.getLatitude(), location.getLongitude()));
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationTextView.setText(String.format("Lat: %s Lon: %s", location.getLatitude(), location.getLongitude()));
    }
}
