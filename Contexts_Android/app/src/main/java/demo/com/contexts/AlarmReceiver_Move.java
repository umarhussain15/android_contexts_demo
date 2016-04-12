package demo.com.contexts;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Umar on 28-Mar-16.
 */
public class AlarmReceiver_Move extends BroadcastReceiver  {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp= context.getSharedPreferences(
                context.getResources().getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        if (sp.getBoolean("notify", false)) {
            Intent alarmRepeat_Move = new Intent(context, AlarmReceiver_Move.class);
            PendingIntent pendingIntent2 =
                    PendingIntent.getBroadcast(context, 0, alarmRepeat_Move, 0);
            AlarmManager alarmManager =
                    (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent2);
            // Notification for telling user to move is allowed by Location Service
            Log.e("Move Alarm Entery","Move Alarm Received TRUE");
            Intent mainIntent = new Intent(context, MainActivity.class);

            NotificationManager notificationManager
                    = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Build the notification with required elements and appropriate data
            Notification noti = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0, mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle("You have class @"+intent.getStringExtra("hour")
                            +":"+intent.getStringExtra("minute")
                            +" "+intent.getStringExtra("ampm"))

                    .setContentText("You are " + sp.getLong("distance", -1) + " km away")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("ticker message")
                    .setWhen(System.currentTimeMillis())
                    .build();
            // show the notification
            notificationManager.notify(1, noti);

            Intent repeatAlarmIntent = new Intent(context, AlarmReceiver_Move.class);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(context, 0, repeatAlarmIntent, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // when setting alarm manger time we will use RTC_Wakeup so that
                // it fires alarm even if device is insleep(screen off)
                // and RTC is the time reference of the device
                // it accept time in milliseconds with reference to device time
                //it fires alarm unless the notification has been displayed
                Log.d("Move Alarm", ((sp.getInt("travelTime", 0) / 2) * 60000) + "");
                if((sp.getInt("travelTime", 0)/2)> sp.getInt("interval",0)) {
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
//                        ((sp.getInt("travelTime", 0)/2)*60000), pendingIntent);
                }
            }
        }
        else {
            // Repeat Alarm to Check if notify condition is true or not
            Log.e("Move Alarm Entery", "Move Alarm Received FALSE");
            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            Intent repeatAlarmIntent = new Intent(context, AlarmReceiver_Move.class);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(context, 0, repeatAlarmIntent, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // when setting alarm manger time we will use RTC_Wakeup so that
                // it fires alarm even if device is insleep(screen off)
                // and RTC is the time reference of the device
                // it accept time in milliseconds with reference to device time
                //it fires alarm unless the notification has been displayed
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        50000, pendingIntent);
            }

        }
    }
}
