package demo.com.contexts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
/**
 * Created by Umar on 18-Mar-16.
 */

// the service class will also be declared in manifest file so that O.S can handle it.
public class ShowNotification extends Service {
    private final static String TAG = "ShowNotification";

    // when service start this method is called
    @Override
    public void onCreate() {
        super.onCreate();
}

    // after onCreate onStartCommand is called
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent mainIntent = new Intent(this, MainActivity.class);

        // Initialize Notification Manger instance
        NotificationManager notificationManager
                = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build the notification with required elements and appropriate data
        Notification noti = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle("Class in " +
                        getSharedPreferences(getResources().getString(R.string.shared_pref_key),MODE_PRIVATE)
                                .getInt("travelTime",0)+" minutes")

                .setContentText(intent.getStringExtra("name")+
                        " @ "+ intent.getStringExtra("hour")+
                        ":"+intent.getStringExtra("minute")+
                        " "+intent.getStringExtra("ampm"))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("ticker message")
                .setWhen(System.currentTimeMillis())
                .build();
        // show the notification
        notificationManager.notify(0, noti);

        // then check if the silent boolean is ture
        // Silent the device after 1 second delay
        // for delay handler is required other wise it will not wait
        if (getSharedPreferences(getResources()
                .getString(R.string.shared_pref_key),MODE_PRIVATE)
                .getBoolean("silent",false)){
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    AudioManager audiomanage =
                            (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
            }, 10000);

        }
        Log.i(TAG, "Notification created");
        // stop service after task is completed
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
