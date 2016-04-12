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
 * Created by Umar on 18-Mar-16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    // this alarm receiver has to be registered in manifest so that O.S can handle the alarm broadcast
    // when alarm manger fires the pending intent this class will call on receive method of broadcast Receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        // Since the notification can be shown up irrespective of application runing or not
        // we need to start a service which will display notification for us and then will stop

        //Simple Class notification i.e.
        // Class is about to start in SOME minutes
        Intent i = new Intent(context, ShowNotification.class);
        // necessary data passed to service
        i.putExtra("name", intent.getStringExtra("name"));
        i.putExtra("hour", intent.getStringExtra("hour"));
        i.putExtra("minute", intent.getStringExtra("minute"));
        i.putExtra("ampm", intent.getStringExtra("ampm"));
        Log.e("DATA CHECK", intent.getStringExtra("name") +
                " @ " + intent.getStringExtra("hour") +
                ":" + intent.getStringExtra("minute") +
                " " + intent.getStringExtra("ampm"));
        // starting service
        context.startService(i);


    }
}
