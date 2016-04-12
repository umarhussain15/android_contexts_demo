package demo.com.contexts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Umar on 11-May-15.
 */
public class AlarmReceiver_Location extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("received", "Alarm rec");
        SharedPreferences sp =  context.getSharedPreferences("demo.com.contexts.sharedprefs.key.123"
                ,Context.MODE_PRIVATE);

        if (isNetworkAvailable(context)) {
            try {

                // start location service
                if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) ==
                        ConnectionResult.SUCCESS) {
                    context.startService(new Intent(context, BGServiceLocation.class));
                }
                //if it is time to start moving for class, show notification and stop the service
                if(sp.getBoolean("notify",false)) {
                     Intent i = new Intent(context, ShowNotification.class);
                     i.putExtra("name", "Class ");
                     i.putExtra("hour", sp.getString("hour", "-1"));
                     i.putExtra("minute", sp.getString("minute","-1"));
                     i.putExtra("ampm", sp.getString("ampm", "-1"));
                    i.putExtra("travelTime", sp.getInt("travelTime",0));

                    Log.e("DATA CHECK", "Class" + " @ " + intent.getStringExtra("hour") + ":" +
                             intent.getStringExtra("minute") + " " + intent.getStringExtra("ampm"));
                    // starting notification service to display notifition
                     //context.startService(i);
                    //stops the service that calculates distance, time location
                    context.stopService(new Intent(context, BGServiceLocation.class));
                }


                else {

                    // Fire this alarm again; repeating effect
                    AlarmManager alarmManager = (AlarmManager)
                            context.getSystemService(Context.ALARM_SERVICE);
                    Intent gpsTrackerIntent = new Intent(context, AlarmReceiver_Location.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        // when setting alarm manger time we will use RTC_Wakeup so that
                        // it fires alarm even if device is insleep(screen off)
                        // and RTC is the time reference of the device
                        // it accept time in milliseconds with reference to device time
                        //it fires alarm unless the notification has been displayed
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 50000, pendingIntent);
                    }
                }

            } catch (Exception e) {
               // context.startService(new Intent(context, BGLocationManager.class));
            }
        }
    }

    // Network check if connected or not
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
