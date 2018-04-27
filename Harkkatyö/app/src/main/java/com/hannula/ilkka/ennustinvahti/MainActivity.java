package com.hannula.ilkka.ennustinvahti;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    private double raja = 1.6;

    AsyncTask<Void, Void, Void> mTask;
    String EnnustejsonString;
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
                mTask.execute();
            }
        });

        mTask = new AsyncTask<Void, Void, Void> () {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    EnnustejsonString = getJsonFromServer(forecastURL);
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

                Ennuste ennuste = null;
                try {
                    ennuste = new Ennuste(EnnustejsonString, nousut, laskut);
                    if (ennuste.getKorkeus() > raja){
                        esitysTextView.setText(ennuste.muodostaTeksti());
                    }
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
    private static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(dc.getInputStream()));

        return inputStream.readLine();
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
}
