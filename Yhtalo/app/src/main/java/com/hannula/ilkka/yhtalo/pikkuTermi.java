package com.hannula.ilkka.yhtalo;


import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class pikkuTermi extends LinearLayout {

    private TextView etumerkki;
    private TextView luku;
    private boolean valittuna;

    public pikkuTermi(Context context){
        super(context);
    }

    public pikkuTermi(String etumerkki, String luku, Context context){
        this(context);
        this.etumerkki = Termi.luoTextView(context, etumerkki);
        this.etumerkki.setPadding(0,0,0,0);
        this.addView(this.etumerkki);
        this.luku = Termi.luoTextView(context,luku);
        this.addView(this.luku);
        this.luku.setOnClickListener(valitse);
        valittuna = false;
    }

    public void poistaValinta(){
        //TODO jos kaipaa hifia valittuna = false;
        etumerkki.setVisibility(VISIBLE);
    }

    public String getEtumerkki(){
        return etumerkki.getText().toString();
    }

    public void piilotaEtumerkki(){
        etumerkki.setVisibility(GONE);
    }

    public void vaihdaEtumerkki(String merkki){
        etumerkki.setText(merkki);
        etumerkki.setVisibility(VISIBLE);
    }



    View.OnClickListener valitse = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView tama = (TextView) v;
            if (tama.getTypeface() == null) {
                tama.setTypeface(null, Typeface.BOLD);
                valittuna = true;
            } else {
                tama.setTypeface(null);
                valittuna = false;
            }
        }
    };
}
