package demo.com.contexts;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Umar on 23-Mar-16.
 */
public class BGServiceLocation extends Service implements LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener {
    private boolean service_status = false;
    Location myLocation;
    private GoogleApiClient myGoogleApiClient; // Google client to interact with Google API
    public double lat,lat2;
    public double lng,lng2;
    Intent intent;
     SharedPreferences sp;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!service_status) {
            service_status = true;
            checkGooglePlayService();
        }
        return START_NOT_STICKY;
    }

    private void checkGooglePlayService() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            myGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            if (!myGoogleApiClient.isConnected() || !myGoogleApiClient.isConnecting()) {
                myGoogleApiClient.connect();
            }
        } else {
            Log.e("checkGooglePlayService", "unable to connect to google play services.");
        }
    }

    protected void stopLocationUpdates() {
        if (myGoogleApiClient != null && myGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    myGoogleApiClient, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            lat = myLocation.getLatitude();
            lng = myLocation.getLongitude();
             sp= getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                     Context.MODE_PRIVATE);
            lat2=Double.parseDouble(sp.getString("lat", "-1"));
            lng2=Double.parseDouble(sp.getString("lng","-1"));
            Log.e("Coord", lat + "'" + lng);
            Log.e("Distance",distance(lat,lng,lat2,lng2,"K")+" km");
            Calendar c=Calendar.getInstance();
            Date d1 = c.getTime();//current time from calander

            String classHr = sp.getString("hour","-1");
            String classMin = sp.getString("minute","-1");
            String classampm = sp.getString("ampm","-1");
            String classDay = sp.getString("day","-1");
            String classMonth = sp.getString("month","-1");
            String classYear = sp.getString("year", "-1");

            String dateEnd = classDay+
                    "/"+classMonth+
                    "/"+classYear+
                    " "+classHr+
                    ":"+classMin+
                    " "+classampm;//time of class taken from sp

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date d2 = null;
            try {
                d2 = format.parse(dateEnd);//parsing class Time to standard time format
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // Get msec from each, and subtract.
            long diff = d2.getTime() - d1.getTime();
            Log.e("Timings ", String.valueOf(d2.getTime() - d1.getTime()));

            boolean move = startMoving(distance(lat, lng, lat2,lng2,"K"),
                                                    d2.getTime(),d1.getTime());
            Log.e("To move", String.valueOf(move));
            // weather its time to move or not
            if(move){
                //setting notify to true so that notificaton can be sent
                sp.edit().putBoolean("notify", move).commit();
            }
            stopSelf();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            stopLocationUpdates();
            stopSelf();
        }
    }

    // This function calculates if user should start moving towards the destination or not

    private  boolean startMoving(double distance,long classTime, long currentTime){
        long travelTime=0;
        //average human walking speed is about
        // 5 kilometres per hour = 1.3889e-6 km/milliseconds
        double onFoot = 1.3889e-6;

        //standard driving speed in urban areas is
        // 50 kilometres per hour = 1.3889e-5 km/milliseconds
        double onVehicle = 1.3889e-5;

        if(distance <= 1){
            //travel time is given in terms of on foot walk (in milliseconds)
            // if user is within 1km radius of destination
            travelTime= (long) (distance/onFoot);
            Log.e("Travel Time on foot ", String.valueOf(travelTime));
        }
        else{
            //travel time is given in terms of traveling by a vehicle (in milliseconds)
            travelTime= (long) (distance/onVehicle);
            Log.e("Travel Time in vehicle", String.valueOf(travelTime));

        }
        Log.e("If condition ","  class time: "+classTime+" Current time: "+currentTime +
                " classtime - currenttime: "+
                (classTime-currentTime)+" travel time: "+ travelTime );

        if(classTime-currentTime <= travelTime){

            sp.edit().putInt("travelTime", (int)(travelTime/60000))
                    .putLong("distance", (long) distance)
                    .commit();
            return true;//user must start moving now
        }
        return false;
    }

    private static double distance(double lat1, double lon1, double lat2,
                                                    double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) *
                Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) *
                        Math.cos(deg2rad(lat2)) *
                        Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
