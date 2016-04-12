package demo.com.contexts;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
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
 * Created by Iffat on 3/23/2016.
 */

public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /*
        * Initialize AlarmManager
        * The Alarm Manger is responsible for triggering event after certain amount of time
        * */
        // alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        /*
        * Get current date from calender object to set date
        * in DatePicker
        * */
        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int curYear = c.get(Calendar.YEAR);

        /*
        * Create and return a new instance of TimePickerDialog
        * System Default  TimePickerDialog will show up
        * */
        return new DatePickerDialog(getActivity(),this, curYear, month,day);
        //TO_DO - SET CURRENT DATE AS DEFAULT DATE IN DATE PICKER
    }
    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        /*
        * This method will be called when user select date and press OK
        * It return day, month,year in the format dd/mm/yyyy
        * We will cast the incoming values accordingly
        * */

        /*Now Setting date for alarm manger; when to fire the event
        * After getting date from user we will subtract interval
        * so that notification fires at right time
        * */

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, (monthOfYear+1));
        calendar.set(Calendar.YEAR, year);
        Log.e("values of selection",dayOfMonth+" "+(monthOfYear+1)+" "+year);
        // Store Date in shared preferences to access later
        SharedPreferences sp =
                getActivity().getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                                Context.MODE_PRIVATE);
        sp.edit().putBoolean("notify",false).commit();
        sp.edit().putString("day",dayOfMonth+"")
                .putString("month", (monthOfYear+1) + "")
                .putString("year", year + "").commit();
        // Now Start Time Picker to Select time of class
        DialogFragment dateFragment = new TimePicker();

        dateFragment.show(getFragmentManager(), "TimePicker");
    }
}
