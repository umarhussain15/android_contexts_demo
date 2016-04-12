package demo.com.contexts;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Umar on 19-Mar-16.
 */
public class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    /*
    * Implementing TimePickerDialog.OnTimeSetListener so that when user picks time we can
    * respond to it
    * */

    int day,month,year;
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    SharedPreferences sp;
    public TimePicker() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        sp=getActivity().getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                Context.MODE_PRIVATE);
        day=Integer.parseInt(sp.getString("day", "-1"));
        month=Integer.parseInt(sp.getString("month","-1"));
        year=Integer.parseInt(sp.getString("year","-1"));
        /*
        * Initialize AlarmManager
        * The Alarm Manger is responsible for triggering event after certain amount of time
        * */
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        /*
        * Get current time from calender object to set time
        * in TimePicker
        * */
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        /*
        * Create and return a new instance of TimePickerDialog
        * System Default  TimePickerDialog will show up
        * */
        return new TimePickerDialog(getActivity(),this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(android.widget.TimePicker timePicker, int hourOfDay, int minute) {
        /*
        * This method will be called when user select time and press OK
        * It return hourOfDay(24 hour format) and minutes as Integers.
        * We will cast the incoming values accordingly
        * */
        sp.edit().putBoolean("notify",false).commit();
        TextView tv = (TextView) getActivity().findViewById(R.id.tvnxtclassdate);
        String aMpM = "AM";
        if(hourOfDay >11)
        {
            aMpM = "PM";
        }

        //Make the 24 hour time format to 12 hour time format
        int currentHour;
        if (hourOfDay > 12) {
            currentHour = hourOfDay - 12;
        }
        else
        {
            currentHour = hourOfDay;
        }
        //Display the user changed time on TextView
        tv.setText(String.valueOf(day) +
                " / " + String.valueOf(month) +
                " / " + String.valueOf(year) +
                " @ "+String.valueOf(currentHour) +
                " : " + String.valueOf(minute) +
                " " + aMpM + "\n");
        /*Now Setting time for alarm manger; when to fire the event
        * After getting time from user we will subtract interval
        * so that notification fires at right time
        * */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,month);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        int interval= getActivity()
                .getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                        Context.MODE_PRIVATE).getInt("interval", 5)*60000;

        long timer=calendar.getTimeInMillis()-interval;
        if (timer <0){
            timer =calendar.getTimeInMillis();
        }
        Log.e("millesec", timer + "   ");
        /*
        * Creating Intent for Alarm Receiver which will be called by alarm manager
        * and putting information in it which will be used by AlarmReceiver
        *
        * 1-This one is pre-class notification when Class is about to start
        * */
        Intent myIntent = new Intent(getActivity(), AlarmReceiver.class);
        myIntent.putExtra("name","Some Class");
        myIntent.putExtra("hour",currentHour+"");
        myIntent.putExtra("minute",minute+"");
        myIntent.putExtra("ampm", aMpM + "");
        myIntent.putExtra("notification_type",0);
        // alarm manger uses pending intent which hold the actual intent
        // and broadcast it when time reaches and invoke receive method of Alarm Receiver
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // when setting alarm manger time we will use RTC_Wakeup so that
            // it fires alarm even if device is insleep(screen off)
            // and RTC is the time reference of the device
            // it accept time in milliseconds with reference to device time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timer, pendingIntent);
        }

        /* Store exact time of class in milliseconds*/
        sp.edit().putString("hour",currentHour+"").putString("minute",minute+"")
                .putString("ampm",aMpM)
                .putLong("millitime",timer).commit();
        Log.e("Setting BOO: ", sp.getString("hour","-1")+" : "+sp.getString("minute","-1"));


        /*
        * 2-Create Alarm which will fire after 1 minute to check the current user location
        * and destination, and also checking how much time left*/
        Intent gpsTrackerIntent = new Intent(getActivity(), AlarmReceiver_Location.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(getActivity(), 1, gpsTrackerIntent, 0);

        // Fire the alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // when setting alarm manger time we will use RTC_Wakeup so that
            // it fires alarm even if device is insleep(screen off)
            // and RTC is the time reference of the device
            // it accept time in milliseconds with reference to device time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 50000, pendingIntent);
        }
        /*
        * 3- Waiting for distance and time measure to match
        * */
        Intent Notifier = new Intent(getActivity(), AlarmReceiver_Move.class);
        Notifier.putExtra("name","Some Class");
        Notifier.putExtra("hour",currentHour+"");
        Notifier.putExtra("minute",minute+"");
        Notifier.putExtra("ampm", aMpM + "");
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getActivity(), 0, Notifier, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // alarm manger for notifying user when he is far away from class location
            // and time is less
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 50000, pendingIntent2);
        }
    }

}
