package com.hannula.ilkka.ennustinvahti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

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

public class EnnusteService extends JobService {

    private double raja = 1.6;
    private AsyncTask mBackgroundTask;
    private String EnnustejsonString;
    private String forecastURL = "https://app.fcoo.dk/metoc/v2/data/timeseries?variables=WaveHeight2D,WavePeriod&lat=61.6&lon=21.25";
    private Date[] nousut = new Date[3];
    private Date[] laskut = new Date[3];

    private static final int NOTIFICATION_ID = 104;
    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = EnnusteService.this;


                Ennuste ennuste = null;
                try {
                    EnnustejsonString = getJsonFromServer(forecastURL);
                    haeNousuajat();
                    ennuste = new Ennuste(EnnustejsonString, nousut, laskut);
                    //sendNotification(ennuste.muodostaTeksti());
                    Log.d("asd", ennuste.muodostaTeksti());
                    if (ennuste.getKorkeus() > raja){
                        //if edellisesta tarpeeksi kauan -> notifikaatio
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                Log.i("TAG", "onStartJob");
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                /* false means, that job is done. we don't want to reschedule it*/

                jobFinished(jobParameters, true);
                Log.i("TAG", "onStartJob- OnPost");
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
        Log.i("TAG", "onStopJob");
        return true;
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
            String jsonStr = getJsonFromServer(alkuURL+aika);
            JSONObject jsonObject = new JSONObject(jsonStr);

            nousut[i] = sdf.parse(jsonObject.getJSONObject("results").getString("sunrise"));
            laskut[i] = sdf.parse(jsonObject.getJSONObject("results").getString("sunset"));
        }
    }

    public void sendNotification(String text, Context context) {

        //Context context = EnnusteService.this;

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Primary",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.asd_kuva)
                .setContentTitle("Aaltoja tulossa")
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        /*ntent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidauthority.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}