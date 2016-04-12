package demo.com.contexts;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TimeNotification extends AppCompatActivity implements View.OnClickListener {
    private static final String NOTIFICATION = "demo.android.action.notification.broadcast";
    public final static int REQUEST_CODE_C = 2;
    // References to UI
    Button BTime, BInterval, BDate, BStop;
    Switch Silent;
    TextView interval_val,nextclass_time, nextclass_date;
    // shared preferences to store small data
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_notification);
        setupActionBar();
        setTitle("Class Notification");

        BTime=(Button)findViewById(R.id.btnclasslocation);
        BTime.setOnClickListener(this);
        BInterval=(Button)findViewById(R.id.btninterval);
        BInterval.setOnClickListener(this);
        BDate=(Button)findViewById(R.id.btnclassdate);
        BDate.setOnClickListener(this);
        BStop=(Button)findViewById(R.id.btnstop);
        BStop.setOnClickListener(this);

        interval_val=(TextView)findViewById(R.id.tvinterval);
        nextclass_time=(TextView)findViewById(R.id.tvnxtclass);
        nextclass_date=(TextView)findViewById(R.id.tvnxtclassdate);


        Silent=(Switch)findViewById(R.id.silentswitch);
        // get value from shared prefs for silent boolean and
        // interval (time before class to show notification)
        // if "silent" does not exist the default value will be given false
        sp=getSharedPreferences(getResources().getString(R.string.shared_pref_key),MODE_PRIVATE);
        interval_val.setText(sp.getInt("interval",5)+"");
        Silent.setChecked(sp.getBoolean("silent",false));
        Silent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // the isChecked will be
                // true if the switch is in the On position
                // store the value in shared prefs for using during alarm call
                Log.e("switched",isChecked+"");
                getSharedPreferences(getResources().getString(R.string.shared_pref_key),MODE_PRIVATE)
                        .edit().putBoolean("silent",isChecked).commit();
            }
        });
    }
    // displaying back button on AppBar
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnclasslocation:
                /*
                * First Call Map Activity to select location of class
                *
                * */
                Intent i= new Intent(TimeNotification.this,MapLocationSelection.class);
                startActivityForResult(i,REQUEST_CODE_C);

                break;
            case R.id.btnclassdate:
                /*
                * Next user selects time and date for the class
                * */
                if (!nextclass_time.getText().toString().equals("No Location!")) {
                    DialogFragment dateFragment = new DatePicker();
                    dateFragment.show(getFragmentManager(), "DatePicker");
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Select Location First!",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btninterval:
//                 Show NumberPicker Dialog for setting
//                 interval needed before class start to
//                 notify.
                DialogFragment Number = new NumberPickerDialog();
                Number.show(getFragmentManager(),"NumberPickerDialog");
                break;
            case R.id.btnstop:
                //stop service
                SharedPreferences sp =
                        this.getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                        Context.MODE_PRIVATE);
                //changing notify value to false so that alarm receiver does not
                // start this service again
                sp.edit().putBoolean("notify", false).commit();

                Context context = getBaseContext();
                Intent gpsTrackerIntent = new Intent(context, AlarmReceiver_Location.class);
                Intent alarmRepeat = new Intent(context, AlarmReceiver.class);
                Intent alarmRepeat_Move = new Intent(context, AlarmReceiver_Move.class);

                PendingIntent pendingIntent0 = PendingIntent.getBroadcast(context, 1, gpsTrackerIntent, 0);
                PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, alarmRepeat, 0);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, alarmRepeat_Move, 0);
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent1);
                alarmManager.cancel(pendingIntent0);
                alarmManager.cancel(pendingIntent2);
                stopService(new Intent(this, BGServiceLocation.class));//stopping the service
                Log.e("Service Stopped", "onClick: stopping service");
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {

            case REQUEST_CODE_C:
               // when map activity finishes set text view
                nextclass_time.setText("Latitude: "+data.getDoubleExtra("lat",-1)+
                        "\n"+"Longitude: "+data.getDoubleExtra("lng",-1));

                break;
        }
    }
}
