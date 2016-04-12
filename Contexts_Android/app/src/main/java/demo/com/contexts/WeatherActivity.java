package demo.com.contexts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // LogCat tag

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location myLocation;
    private String API_URL= "http://api.wunderground.com/api/INSERT_YOUR_API_KEY/geolookup/conditions/forecast/q/";

    private GoogleApiClient myGoogleApiClient; // Google client to interact with Google API
    public double lat;
    public double lng;
    ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weathershow);
        // Progress Dialog shown to user when
        // loading data from Weather API
        mProgressDialog= new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        mProgressDialog.show();

        setupActionBar();
        setTitle("WeatherActivity");
        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                mProgressDialog.hide();
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

                finish();
            }
            return false;
        }
        return true;
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
        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("failure", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        try {
            displayLocation();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        myGoogleApiClient.connect();
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() throws ExecutionException, InterruptedException {

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

        if (myLocation != null) {
            lat = myLocation.getLatitude();
            lng = myLocation.getLongitude();
            Toast.makeText(getApplicationContext(), lat + ", " + lng, Toast.LENGTH_LONG).show();
            Log.e("Coord", lat + "'" + lng);

            //Send request to WeatherUnderground via an Async Task
            WeatherAsyncTask tsk = new WeatherAsyncTask(API_URL+lat+","+lng+".json");
            //Get the response JSON string from the Async Task
            String weatherInfo = tsk.execute().get();

            if(weatherInfo!=null) {
               //show the UI for Weather Forecast after data has been fetched

                UpdateUI(weatherInfo);  //Update UI
                mProgressDialog.hide();
            }
            else {
                mProgressDialog.hide();
                Log.e("No Data","No data fetched from api");
                return;
            }
        }
        else {
            Log.e("Location fail", "(Couldn't get the location." +
                    " Make sure location is enabled on the device)");
        }

    }

    /**
     * Parses the JSON string to JSON objects and gets the key value pairs
     * Sets the values in the specified fields on the UI
     */

     private void UpdateUI(String str)  {
         try{
             JSONObject jsonObj = new JSONObject(str);
             JSONObject current_observation = jsonObj.getJSONObject("current_observation");
             String weather = current_observation.getString("weather");
             String temp_f = current_observation.getString("temp_f") + " F";
             String img = current_observation.getString("icon_url");

             JSONObject observation_location =
                     current_observation.getJSONObject("observation_location");

             Log.e("info", weather + " " + temp_f);
             TextView fahrenTextView = (TextView) findViewById(R.id.fahrenTextView);
             TextView celsiusTextView = (TextView) findViewById(R.id.celsiusTextView);
             TextView cityTextView= (TextView) findViewById(R.id.cityTextView);
             TextView countryTextView= (TextView) findViewById(R.id.countryTextView);
             TextView skiesTextView= (TextView) findViewById(R.id.skiesTextView);
             TextView humidityTextView= (TextView) findViewById(R.id.humidityTextView);
             TextView observationTextView= (TextView) findViewById(R.id.observationTextView);

            /***************** Forecst for Today *************************************/
             cityTextView.setText(observation_location.getString("city") );
             countryTextView.setText(observation_location.getString("country") );
             fahrenTextView.setText(current_observation.getString("temp_f") + " F");
             celsiusTextView.setText(current_observation.getString("temp_c") + " C");
             skiesTextView.setText(current_observation.getString("weather") );
             observationTextView.setText(current_observation.getString("observation_time") );
             humidityTextView.setText(current_observation.getString("relative_humidity") );

             TextView day1= (TextView) findViewById(R.id.day1TextView);
             TextView day2= (TextView) findViewById(R.id.day2TextView);
             TextView day3= (TextView) findViewById(R.id.day3TextView);

             TextView descDay1= (TextView) findViewById(R.id.descDay1);
             TextView descDay2= (TextView) findViewById(R.id.descDay2);
             TextView descDay3= (TextView) findViewById(R.id.descDay3);

             /********** Forecast for next 3 days **********************/
             JSONObject forecast = jsonObj.getJSONObject("forecast");
             JSONObject foretxt = forecast.getJSONObject("txt_forecast");
             JSONArray forecastArray =  foretxt.getJSONArray("forecastday");
             String icon, title,desc;

             /************************  DAY 1 ***************************/
             JSONObject childJSONObject = forecastArray.getJSONObject(2);
                 icon = childJSONObject.getString("icon");
                 title  = childJSONObject.getString("title");
                 desc = childJSONObject.getString("fcttext");
             day1.setText(title );
             descDay1.setText(desc );

             /************************  DAY 2 ***************************/
             childJSONObject = forecastArray.getJSONObject(4);
                 icon = childJSONObject.getString("icon");
                 title  = childJSONObject.getString("title");
                 desc = childJSONObject.getString("fcttext");
             day2.setText(title );
             descDay2.setText(desc);

             /************************  DAY 3***************************/
             childJSONObject = forecastArray.getJSONObject(6);
                 icon = childJSONObject.getString("icon");
                 title  = childJSONObject.getString("title");
                 desc = childJSONObject.getString("fcttext");
             day3.setText(title );
             descDay3.setText(desc);


         }
         catch(JSONException jex){
             jex.printStackTrace();
         }

     }



}

