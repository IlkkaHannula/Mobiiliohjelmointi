package com.hannula.ilkka.ennustinvahti;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView esitysTextView;
    private Button hakuButton;

    AsyncTask<Void, Void, Void> mTask;
    String jsonString;
    String forecastURL = "https://app.fcoo.dk/metoc/v2/data/timeseries?variables=WaveHeight2D,WavePeriod&lat=61.6&lon=21.25";
    Date[] nousut = new Date[3];
    Date[] laskut = new Date[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        esitysTextView = findViewById(R.id.esitysTextView);
        hakuButton = findViewById(R.id.hakuButton);

        hakuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                haeEnnusteet();
            }
        });

        mTask = new AsyncTask<Void, Void, Void> () {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    jsonString = getJsonFromServer(forecastURL);
                    try {
                        haeNousuajat();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //TODO muuta palauttamaan jokin olio ja siirrä toiseen luokkaan...
                try {
                    esitysTextView.setText(kasitteleEnnusteet(jsonString));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }

        };
    }

    private void haeNousuajat() throws JSONException, ParseException, IOException {
        String alkuURL = "https://api.sunrise-sunset.org/json?lat=61.6&lng=21.25&formatted=0&date=";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
        //dynamic aurinkoDatat;
        for (int i = 0; i < 3; i++){
            Calendar day = Calendar.getInstance();
            day.setTime(new Date());
            day.add(Calendar.DATE, i);

            String aika = new SimpleDateFormat("yyyy-MM-dd").format(day.getTime());

            //Log.d("ASD", alkuURL+aika);
            String jsonStr = getJsonFromServer(alkuURL+aika);
            JSONObject jsonObject = new JSONObject(jsonStr);


            nousut[i] = sdf.parse(jsonObject.getJSONObject("results").getString("sunrise"));
            laskut[i] = sdf.parse(jsonObject.getJSONObject("results").getString("sunset"));
        }

    }

    public void haeEnnusteet(){
        mTask.execute();
    }

    public String kasitteleEnnusteet(String jsonString) throws JSONException, IOException, ParseException {
        double raja = 1.6;

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray times = jsonObject.getJSONObject("WavePeriod").getJSONObject("TMN").getJSONArray("time");
        JSONArray periods = jsonObject.getJSONObject("WavePeriod").getJSONObject("TMN").getJSONArray("data");
        JSONArray v = jsonObject.getJSONObject("WaveHeight2D").getJSONObject("v").getJSONArray("data");
        JSONArray u = jsonObject.getJSONObject("WaveHeight2D").getJSONObject("u").getJSONArray("data");


        double maxHeight = -1;
        double period = -1;
        double direction = -1;
        Date huippu = new Date();
        int j = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        for (int i = 0; i < times.length(); i++)
        {
            Date kasiteltavaAika = sdf.parse(times.getString(i));

            double uComponent = u.getDouble(i);
            double vComponent = v.getDouble(i);

            if (nousut[j].compareTo(kasiteltavaAika) <= 0 && laskut[j].compareTo(kasiteltavaAika) >= 0 ){
                double height = Math.sqrt(Math.pow(uComponent,2) + Math.pow(vComponent,2));
                if (height > maxHeight){
                    maxHeight = height;
                    period = periods.getDouble(i);
                    huippu = kasiteltavaAika;

                    direction = Math.atan2(-uComponent,-vComponent) * 180/Math.PI;
                    if (direction < 0){
                        direction += 360;
                    }
                }
            }else if(laskut[j].compareTo(kasiteltavaAika) <= 0){
                j += 1;
            }
            if (j == 2){
                break;
            }
        }

        String text = "Korkeus: " + maxHeight + " suunta: " + direction + " periodi: " + period + " huippu: " + huippu;

        return text;
    }

    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(dc.getInputStream()));

        return inputStream.readLine();
    }
}
