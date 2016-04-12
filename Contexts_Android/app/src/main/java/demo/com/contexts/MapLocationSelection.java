package demo.com.contexts;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Umar on 26-Mar-16.
 */
public class MapLocationSelection extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener{


    LocationRequest myLocationRequest;
    GoogleApiClient myGoogleApiClient;
    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker selectedLocationMarker;
    Location myLocation;
    SharedPreferences sp;

    MenuItem ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_loc);
        sp= getSharedPreferences(getResources().getString(R.string.shared_pref_key),MODE_PRIVATE);
        /**
         * This fragment is the simplest way to place a map in an application.
         * It's a wrapper around a view of a map to
         * automatically handle the necessary life cycle needs         *
         */
        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chooselocation, menu);
        ok=menu.findItem(R.id.menu_ok).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ok:
                LatLng ll=selectedLocationMarker.getPosition();
                sp.edit().putBoolean("notify",false).commit();
                sp.edit().putString("lat", ll.latitude+"")
                        .putString("lng", ll.longitude + "").commit();
                Intent resultIntent = new Intent();
                // put data that you want returned to parent Activity
                resultIntent.putExtra("lat",ll.latitude);
                resultIntent.putExtra("lng",ll.longitude);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onMapReady(GoogleMap gMap) {
        mGoogleMap = gMap;

        mGoogleMap.setIndoorEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        buildGoogleApiClient();
        myGoogleApiClient.connect();


        /*
        * Getting Location on map click
        * */
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                mGoogleMap.clear();

                // Animating to the touched position
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                selectedLocationMarker=mGoogleMap.addMarker(markerOptions);
                // Once User has selected the location display Ok button
                ok.setVisible(true);
            }
        });
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
        myLocation = LocationServices.FusedLocationApi
                .getLastLocation(myGoogleApiClient);
        myLocation.setAccuracy(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (myLocation != null) {
            latLng= new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(myLocation.getLatitude() + " , " + myLocation.getLongitude());
            selectedLocationMarker = mGoogleMap.addMarker(markerOptions);

            //zoom to current position:
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(15).build();

            mGoogleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (myGoogleApiClient != null) {
            myGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
