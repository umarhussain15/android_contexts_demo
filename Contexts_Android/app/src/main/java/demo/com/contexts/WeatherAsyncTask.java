package demo.com.contexts;

import android.os.AsyncTask;
import android.util.Log;

import demo.com.contexts.HTTP.HttpCall;

/**
 * Created by Iffat on 3/19/2016.
 */
public class WeatherAsyncTask extends AsyncTask<Void,Void,String> {

    private HttpCall hc = new HttpCall();
    private String uri;
    private String str;
    public WeatherAsyncTask(String API_URL) {
        this.uri = API_URL;
    }
    protected void onPreExecute(){
    }

    protected String doInBackground(Void... arg0) {
        try {
            str =  hc.sendGetRequest(uri);
            //JSONObject weatherObj = hc.getJSON(uri);
            Log.e("url", uri);
            //Log.e("data", str);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String str){

    }
}

