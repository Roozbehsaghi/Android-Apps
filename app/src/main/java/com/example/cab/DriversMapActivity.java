package com.example.cab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
        {
            private GoogleMap mMap;
            GoogleApiClient googleApiClient;
            Location lastLocation;
            LocationRequest locationRequest;

            private Button LogoutDriverButton;
            private Button SettingsDriverButton;
            private FirebaseAuth mAuth;
            private FirebaseUser currentUser;
            private Boolean currentLogOutDriversStatus = false;
            private DatabaseReference AssignedCustomerRef, AssignedCustomerPickUpRef;
            private String driverID, customerID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();

        LogoutDriverButton = (Button) findViewById(R.id.driver_login_btn);
        SettingsDriverButton = (Button) findViewById(R.id.driver_setting_btn);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


 /*       LogoutDriverButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                currentLogOutDriversStatus = true;
                DisconnectTheDrivers();

                mAuth.signOut();
                LogOutDriver();
            }
        });*/

        GetAssignedCustomerRequest();
    }

    private void GetAssignedCustomerRequest()
    {
        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverID).child("CustomerRideID");

        AssignedCustomerRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    customerID = dataSnapshot.getValue().toString();

                    GetAssignedCustomerPickUpLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void GetAssignedCustomerPickUpLocation()
    {
        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Request")
                .child(customerID).child("l");

        AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();

                    double LocationLat = 0;
                    double LocationLng = 0;

                    if (customerLocationMap.get(0) != null)
                    {
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) != null)
                    {
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("PichUp Location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

     //   buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

              return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
         if (getApplicationContext() != null)
         {
             lastLocation = location;

             LatLng latLng = new LatLng(location.getLatitude(), location.getLatitude());
             mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
             mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

             String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

             DatabaseReference DriverAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
             GeoFire geoFireAvailability = new GeoFire(DriverAvailibilityRef);

             DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
             GeoFire geoFireWorking = new GeoFire(DriverAvailibilityRef);

             switch (customerID)
             {
                 case "":
                     geoFireWorking.removeLocation(userID);
                     geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                     break;

                     default:
                         geoFireAvailability.removeLocation(userID);
                         geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                         break;
             }

         }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (!currentLogOutDriversStatus)
        {
            DisconnectTheDrivers();
        }

    }

    private void DisconnectTheDrivers()
    {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Driver Availible");

        GeoFire geoFire = new GeoFire(DriverAvailibilityRef);
        geoFire.removeLocation(userID);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        
    }

    private void LogOutDriver()
    {
        Intent welcomeIntent = new Intent(DriversMapActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
}
