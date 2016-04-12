package demo.com.contexts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

/**
 * AppCompatActivity: You can add an ActionBar to your activity when running on API level 7 or
 * higher by extending this class for your activity
 *
 * OnMapReadyCallback: Callback interface for when the map is ready to be used.
 * GoogleApiClient.ConnectionCallbacks: Provides callbacks that are called when the client is
 *                                      connected or disconnected from the service.
 *                                      Applications implements onConnected(Bundle) to start making
 *                                      requests.
 * GoogleApiClient.OnConnectionFailedListener: Provides callbacks for scenarios that result in a
 *                                              failed attempt to connect the client to the service
 * com.google.android.gms.location.LocationListener: Used for receiving notifications from the
 *                                                      FusedLocationProviderApi
 */
public class CurrLocActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    LocationRequest myLocationRequest;
    GoogleApiClient myGoogleApiClient;
    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker currLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_loc);

        /**
         * This fragment is the simplest way to place a map in an application.
         * It's a wrapper around a view of a map to automatically
         * handle the necessary life cycle needs         *
         */
        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
    }
    /**
     * This method is triggered when the map is ready to be used
     * and provides a non-null instance of GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap gMap) {
        mGoogleMap = gMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setIndoorEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        buildGoogleApiClient();
        myGoogleApiClient.connect();

    }

    protected synchronized void buildGoogleApiClient() {
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //on connection it gets the last available location and moves the pointer to that location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                myGoogleApiClient);
        if (mLastLocation != null) {
            //create a new LatLng obj to store position
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);//set the position of marker to new location
            //set lat, lng to appear in marker title
            markerOptions.title(mLastLocation.getLatitude() + " , " + mLastLocation.getLongitude());
            currLocationMarker = mGoogleMap.addMarker(markerOptions);//show marker on map
        }

        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(10000); // Location will be updated every 10 seconds
        myLocationRequest.setFastestInterval(5000); //5 seconds - If location is available because
                                                    // of some other app running concurrently,
                                                    // it will be updated in 5 seconds
        //prioritize low power usage by device
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //1/2 meter.The smallest displacement in meters the user must move between location updates.
        myLocationRequest.setSmallestDisplacement(0.5F);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Updates location
        LocationServices.FusedLocationApi
                .requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(location.getLatitude() + " , " + location.getLongitude());
        currLocationMarker = mGoogleMap.addMarker(markerOptions);

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

    }

    @Override
    protected void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(myGoogleApiClient, this);
        super.onDestroy();
    }
}