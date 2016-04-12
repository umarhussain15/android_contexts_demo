package demo.com.contexts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

     Button BCurloc,BWeather,BAtms,BNotify;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BCurloc= (Button)findViewById(R.id.btn_currloc);
        BCurloc.setOnClickListener(this);

        BWeather= (Button)findViewById(R.id.btn_weather);
        BWeather.setOnClickListener(this);

        BAtms= (Button)findViewById(R.id.btn_atms);
        BAtms.setOnClickListener(this);

        BNotify= (Button)findViewById(R.id.btn_notification);
        BNotify.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch(view.getId()){
            case R.id.btn_currloc:
                i= new Intent(MainActivity.this,CurrLocActivity.class);
                startActivity(i);
                break;
            case R.id.btn_weather:
                i= new Intent(MainActivity.this,WeatherActivity.class);
                startActivity(i);
                break;
            case R.id.btn_atms:
                i= new Intent(MainActivity.this,NearByAtms.class);
                startActivity(i);
                break;
            case R.id.btn_notification:
                i= new Intent(MainActivity.this,TimeNotification.class);
                startActivity(i);
                break;
            default:

        }
    }
}
