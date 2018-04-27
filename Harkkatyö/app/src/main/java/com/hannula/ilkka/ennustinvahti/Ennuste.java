package com.hannula.ilkka.ennustinvahti;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ennuste {
    private double korkeus;
    private double suunta;
    private double periodi;
    private Date huipunAika;

    Ennuste(String jsonString, Date[] nousut, Date[] laskut) throws JSONException, IOException, ParseException {

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
        this.korkeus = maxHeight;
        this.suunta = direction;
        this.periodi = period;
        this.huipunAika = huippu;
    }

    public double getKorkeus(){
        return korkeus;
    }

    public String muodostaTeksti(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM. HH:mm");
        return "Korkeus: " + String.format("%.2f", korkeus) + " suunta: " + String.format("%.0f", suunta) + " periodi: " + String.format("%.1f", periodi) + " huippu: " + sdf.format(huipunAika);

    }
}
