package com.hannula.ilkka.ennustinvahti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
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

                    if (ennuste.getKorkeus() > EnnustePreferences.haeEnnusteRaja(context)) {
                        notifikoiJosTarve(ennuste.muodostaTeksti(), EnnusteService.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobParameters, true);
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

    public static void notifikoiJosTarve(String text, Context context) {

        long aikaViimeNotifikaatiosta = EnnustePreferences
                .kulunutAikaViimeNotifikaatiosta(context);

        int halytysvali = EnnustePreferences.haeHalytysvali(context);

        if (aikaViimeNotifikaatiosta < DateUtils.HOUR_IN_MILLIS*halytysvali) {
            Log.d("ASD","Notifikaatio lähetetty vasta.");
            return;
        }

        lahetaNotifikaatio(text, context);

        EnnustePreferences.tallennaNotifikaationAika(context, System.currentTimeMillis());
    }

    public static void lahetaNotifikaatio(String text, Context context){
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
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setDefaults(Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.aaltopoiju.fi"));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationBuilder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}