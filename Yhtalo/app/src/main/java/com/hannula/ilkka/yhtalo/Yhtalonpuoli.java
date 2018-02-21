package com.hannula.ilkka.yhtalo;

import android.widget.LinearLayout;

public class Yhtalonpuoli {
    private LinearLayout esitysAlue;
    //TODO etsi fiksumpi sailio
    private Termi[] termit;
    private String puoli;

    public Yhtalonpuoli(LinearLayout esitysAlue, String puoli) {
        this.esitysAlue = esitysAlue;
        this.puoli = puoli;
    }

    private boolean vaikuttaakoViereiset(int indeksi){
        //TODO tarkastele jalkimmaisen etumerkki;
        return false;
    }

    public void lisaaTermi(Termi termi){
        //TODO etsi fiksu sailio
        esitysAlue.addView(termi);
        termi.setPuoli(puoli);
    }

    public int getTermienMaara(){
        return esitysAlue.getChildCount();
    }

    public void summaa(Termi termi){

    }

    public void vahenna(Termi termi){

    }

    public void kerroPuoli(Termi termi){

    }

    public void jaaPuoli(Termi termi){

    }

    public void loytyykoSama(Termi termi){

    }

}
