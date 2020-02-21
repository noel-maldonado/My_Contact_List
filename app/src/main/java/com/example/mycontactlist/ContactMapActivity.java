package com.example.mycontactlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ContactMapActivity extends AppCompatActivity  implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {


    final int PERMISSION_REQUEST_LOCATION = 101;
    GoogleMap gMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    ArrayList<Contact> contacts = new ArrayList<>();
    Contact currentContact = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();
        try {
            ContactDataSource ds = new ContactDataSource(ContactMapActivity.this);
            ds.open();
            if(extras != null) {
                currentContact = ds.getSpecificContact(extras.getInt("contactid"));
            }
            else {
                contacts = ds.getContacts("contactname", "ASC");
            }
            ds.close();
        } catch (Exception e) {
            Toast.makeText(this, "Contact(s) could not be retrieved.", Toast.LENGTH_LONG).show();
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); //used to make map compatible with earlier versions of the Android Operating System
        mapFragment.getMapAsync(this); //map is retrieve asynchronously;  This class automatically initializes the maps system and the view.
        createLocationRequest(); //Calls the method that sets up the location listener

        if (mGoogleApiClient == null) { //API Client is created to establish a connection with API Services
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        initMapTypeButton();
        initListButton();
        initMapButton();
        initSettingsButton();
    }

    protected void onStart() {  //connects to the app to API services
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() { //disconnects API Services; not on onPause so that the APP can monitor location when it is in the background
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void createLocationRequest() { //Location Listener
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(10000); //standard monitoring interval
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //uses GPS to get coordinates
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(), //checks permissions
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return  ; //Stops the method if the permission is not granted
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //location listening using the API Client
    }

    @Override
    public void onConnectionSuspended(int i) { //stops location updates if something something happens to the connection to the API Client
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { //stops location updates if something something happens to the connection to the API Client
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap; //Goggle Maps Object
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Type of Google Maps used

        Point size = new Point();                   //
        WindowManager w = getWindowManager();       // To properly bound a group of points, the app needs to know the size of the display
        w.getDefaultDisplay().getSize(size);        // Code aks the device for the dimensions of the display
        int measuredWidth = size.x;                 //
        int measuredHeight = size.y;                //

        if (contacts.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder(); //constructs the geographic boundaries of a set of GPS coordinates; initiates the builder for use
            for (int i=0; i<contacts.size(); i++) { //loops through the contacts while adding each one to the map
                currentContact = contacts.get(i);

                Geocoder geo = new Geocoder(this);
                List<Address> addresses = null;

                String address = currentContact.getStreetAddress() + ", " +
                        currentContact.getCity() + ", " +
                        currentContact.getState() + " " +
                        currentContact.getZipCode();

                try {
                    addresses = geo.getFromLocationName(address, 1);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                LatLng point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()); //uses the GPS coordinates returned from the geocoding service to instantiate the Latlng Object
                builder.include(point); //point added to the builder where it is considered when creating the map boundaries

                gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName() + " " + currentContact.getPhoneNumber()).snippet(address)); //adds the the standard marker (pin) to the map using the location
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), measuredWidth, measuredHeight, 450));  // tells the map to zoom in to the location of the markers; uses the boundaries set by LatLngBounds
        }
        else { //if Contacts Arraylist does not contain any objects this code is run
            if (currentContact != null) { //checks to see if there is a single contact object to map
                Geocoder geo = new Geocoder(this);
                List<Address> addresses = null;

                //address retrieved
                String address = currentContact.getStreetAddress() + ", " +
                        currentContact.getCity() + ", " +
                        currentContact.getState() + " " +
                        currentContact.getZipCode();

                try {
                    addresses = geo.getFromLocationName(address, 1); //address added to Adresses List
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                LatLng point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()); //uses the GPS coordinates returned from the geocoding service to instantiate the Latlng Object

                gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName() + " " + currentContact.getPhoneNumber()).snippet(address));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16)); // tells the map to zoom in to the location of the markers; uses the boundaries set by LatLngBounds
            }
            else { // if no contacts then this message appears
                AlertDialog alertDialog = new AlertDialog.Builder(ContactMapActivity.this).create();
                alertDialog.setTitle("No Data");
                alertDialog.setMessage("No data is available for the mapping function.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    } });
                alertDialog.show();
            }
        }

        try{
            if(Build.VERSION.SDK_INT >= 23) { //Operating System SDK is checked
                if(ContextCompat.checkSelfPermission(ContactMapActivity.this, //*if the device is running an operating system greater than 22        */
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=          //*checks to see if permission has been previously granted             */
                        PackageManager.PERMISSION_GRANTED) {                                  //*if it has the app will start the code to find the device's location */
                    if (ActivityCompat.shouldShowRequestPermissionRationale(  //If user denied permission, app shows rationale why the app needs the permission
                            ContactMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Snackbar.make(findViewById(R.id.activity_map),  //Snackbar is an object that implements a kind of AlertDialog that is run as an asychronous task floating in the activity's layout
                                "MyContactList requires this permission to locate " +
                                        "your contacts", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() { //Length Indefinite keps the Snackbar open until the user responds by clicking OK or Deny
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(
                                        ContactMapActivity.this,
                                        new String[] {
                                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_LOCATION);
                            }
                        }).show();
                    } else {
                        ActivityCompat.requestPermissions(ContactMapActivity.this, new
                                        String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_LOCATION);
                    }
                } else {
                    startLocationUpdates();
                }
            } else {
                startLocationUpdates();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Error requesting permission", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) { //Displays Location when it changes
        Toast.makeText(getBaseContext(), "Lat: " + location.getLatitude() +
                " Long: " + location.getLongitude() +
                " Accuracy: " + location.getAccuracy(), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    private void initListButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactMapActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    private void initMapButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonMap);
        ibList.setEnabled(false);
    }

    private void initSettingsButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonSettings);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactMapActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }




    private void startLocationUpdates() {
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        gMap.setMyLocationEnabled(true); //Enables the map to find the device location
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //method signature that is required to

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(ContactMapActivity.this, "MyContactList will not locate your contacts.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    private void initMapTypeButton() {
        final Button satelitebtn = (Button) findViewById(R.id.buttonMapType);
        satelitebtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String currentSetting = satelitebtn.getText().toString(); //saves current setting
                if (currentSetting.equalsIgnoreCase("Satellite View")) { //checks to see if current setting is Normal View by referncing saved button text
                    gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); //changes map type
                    satelitebtn.setText("Normal View"); //changes button text to Normal View
                }
                else {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    satelitebtn.setText("Satellite View");
                }
            }
        });
    }

}
