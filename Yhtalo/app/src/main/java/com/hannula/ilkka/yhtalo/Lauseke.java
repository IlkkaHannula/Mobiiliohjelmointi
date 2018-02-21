package com.hannula.ilkka.yhtalo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class Lauseke extends LinearLayout {
    private boolean sulut;

    private String[] etumerkit = {"+","-","/","*"};

    private int ID; //voisi tarkastaa, että missä mennään?

    public Lauseke(Context context) {
        super(context);
    }


    public Lauseke(String merkkijono, Context context){
        this(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        otatilaa(this);
        this.setWeightSum(1);

        taytaKuvake(merkkijono, context);

        siistiUlkoasu();

    }

    public void lisaaPikkuTermi(String etumerkki, String luku){
        pikkuTermi uusi = new pikkuTermi(etumerkki, luku, this.getContext());
        this.addView(uusi);
    }

    public int getTermienMaara() {
        return this.getChildCount();
    }


    public void otatilaa(LinearLayout alue) {
        alue.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        alue.setGravity(Gravity.CENTER);
    }

    //TODO rename fiksusti (lauseke tj)?
    public void taytaKuvake(String merkkijono, Context context){
        if (merkkijono.length() > 0){
            String etumerkki;
            String merkki;
            String luku;

            /*String edellinenMerkki = merkkijono.substring(0,1);
            int alku = 0;
            if (Arrays.asList(etumerkit).contains(edellinenMerkki)){
                etumerkki = edellinenMerkki;
                alku = 1;
            }else{
                etumerkki = "+";
                edellinenMerkki = "+";
            }

            for (int i = alku; i < merkkijono.length()-1; i++) {

                merkki = merkkijono.substring(i, i +1);

                if (){

                }

                edellinenMerkki = merkki;
            }*/
            //TODO paloittelu
            pikkuTermi osa = new pikkuTermi("-", merkkijono, context);
            this.addView(osa);
        }

    }

    //TODO tasta ja muista vastaavista yleispatevampi?
    public TextView luouusi(Context context) {
        TextView uusi = new TextView(context);
        uusi.setText("1");
        return uusi;
    }

    public void siistiUlkoasu(){
        for (int i = 0; i < this.getChildCount(); i++){
            ((pikkuTermi) this.getChildAt(i)).poistaValinta();
        }

        if (this.getChildCount() > 0){
            pikkuTermi eka = (pikkuTermi) this.getChildAt(0);
            if (eka.getEtumerkki().equals("+")){
                eka.piilotaEtumerkki();
            }
        } else{
            //TODO joku huomautus, koska näin ei pitäisi käydä
        }
    }


}
