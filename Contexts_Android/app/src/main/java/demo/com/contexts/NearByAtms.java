package demo.com.contexts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import demo.com.contexts.HTTP.HttpCall;

public class NearByAtms extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Location mLastLocation;
    GoogleApiClient myGoogleApiClient;
    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    private AlertDialog ad;
    ProgressDialog mProgressDialog;
    int selected=4;
    List<Marker> atm_markers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);
        // Keep user busy
        mProgressDialog= new ProgressDialog(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Inflate menu with menu layout placed in res/menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.atm_activity_menu, menu);
        return true;
    }
    // Listener for menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if current location button is clicked
            // get current location and set camera
            case R.id.menu_btnrefresh:
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        myGoogleApiClient);
                if (mLastLocation != null) {
                    latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    currLocationMarker.setPosition(latLng);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng).zoom(15).build();
                    mGoogleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
                return true;

            // when user clicks on this option a selection dialog will open up to show
            // different predefined radius values
            case R.id.menu_btnrange:

                // values shown to the user
                // their index will identify which option
                // is clicked
                final String[] choiceList =
                        {"200m", "400m" ,"450m","600m", "800m" , "1600m","2000m" };
                // Create AlertDialog and set title
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Radius");

                // alert dialog gives method to set single choice item
                // and also setting listener for item selection
                // it takes
                // String[] display strings: choiceList
                // int selected : the item index which will be pre selected, -1 for no selection
                // OnClicklistener
                builder.setSingleChoiceItems(choiceList,selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // get the selected item and set corresponding string to that
                                selected=item;
                                String radius="0";
                                switch (item){
                                    case 0:
                                        radius="200";
                                        break;
                                    case 1:
                                        radius="400";
                                        break;
                                    case 2:
                                        radius="450";
                                        break;
                                    case 3:
                                        radius="600";
                                        break;
                                    case 4:
                                        radius="800";
                                        break;
                                    case 5:
                                        radius="1600";
                                        break;
                                    case 6:
                                        radius="2000";
                                }
                                // call to server with dummy data
                                new GetAtms(33.642552+"", 72.990174+"",radius).execute();

                                // Call to server with current location
//                                new GetAtms(mLastLocation.getLatitude()+"",
//                                          mLastLocation.getLongitude()+"",radius).execute();

                               // hide the dialog
                                ad.dismiss();
                            }
                        });
                ad = builder.create();
                ad.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                myGoogleApiClient);
        if (mLastLocation != null) {
            //create a new LatLng obj to store position
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);//set the position of marker to new location
            markerOptions.icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            //set lat, lng to appear in marker title
            markerOptions.title("You @ " + mLastLocation.getLatitude()
                    + " , " + mLastLocation.getLongitude());
            currLocationMarker = mGoogleMap.addMarker(markerOptions);//show marker on map
            //zoom to current position:
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(15).build();
            mGoogleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    // Async Task to get atms coordinates from server
    public class GetAtms extends AsyncTask<Void, Void, JSONObject> {

        // current location and searching radius will be sent via POST CALL
        String lat,lng,radius;
        GetAtms(String lat,String lng,String radius) {
            this.lat=lat;
            this.lng=lng;
            this.radius=radius;
        }

        @Override
        protected void onPreExecute() {
//            super.onPreExecute();
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            //preparing post elements
            HashMap<String, String> paras = new HashMap<>();
            paras.put("lat", lat);
            paras.put("lng", lng);
            paras.put("radius", radius);
            // Calling post function from HttpCall class
            JSONObject json = new HttpCall().postForJSON(getResources().getString(R.string.fetch_atms), paras);
            try {
                if (json==null)
                    return  null;
                int success=json.getInt("success");
                Log.i("suc val int", "" + success);
                if (success==-2)
                    return null;
                if (success==-1)
                    return null;
                // if we get successful JSON back then we will update UI
                // in onPostExecute task
                return json;
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject success) {
            mProgressDialog.hide();
            Log.i("success val", success.toString());
            if (success==null){
                Toast.makeText(getApplicationContext(),
                        "Error Occurred! Try Again", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(),
                        "Successfully updated!",Toast.LENGTH_SHORT).show();


                try {
                    // before setting new data, first remove the old markers
                    for(int i=0;i<atm_markers.size();i++){
                        atm_markers.get(i).remove();
                    }
                    // also clear the holding list
                    atm_markers.clear();

                    // read json array from JSONObject
                    JSONArray ja= success.getJSONArray("results");
                    for (int i=0;i<ja.length();i++){
                        // for each array entry create new marker and
                        // show that on the map and store in arraylist of markers
                        JSONObject result = ja.getJSONObject(i);
                        //create a new LatLng obj to store position
                        LatLng temp = new LatLng(result.getDouble("lat"), result.getDouble("lng"));
                        MarkerOptions markerOptions = new MarkerOptions();
                        //set the position of marker to new location
                        markerOptions.position(temp);
                        //set name in title
                        markerOptions.title(result.getString("place"));
                        Marker marker =
                                mGoogleMap.addMarker(markerOptions);

                        atm_markers.add(marker);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
        @Override
        protected void onCancelled() {

        }
    }
}
