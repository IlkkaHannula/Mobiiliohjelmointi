package com.hannula.ilkka.yhtalo;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//Toast.makeText(getContext(), clipdata.getItemAt(0).toString(), Toast.LENGTH_SHORT).show();

public class Termi extends LinearLayout{
    private TextView etumerkki;
    private Lauseke osoittaja;
    private Lauseke nimittaja;
    private LinearLayout kehys;

    public String getPuoli() {
        return puoli;
    }

    public void setPuoli(String puoli) {
        this.puoli = puoli;
    }

    private String puoli;

    public Termi(Context context) {
        super(context);
    }

    public Termi(String etumerkki, String osoittaja, String nimittaja, Context context) {
        this(context);

        this.etumerkki = luoTextView(context,etumerkki);
        this.osoittaja = new Lauseke(osoittaja, context);
        this.nimittaja = new Lauseke(nimittaja, context);

        luoLayout(context);
    }

    public String getEtumerkki() {
        return etumerkki.getText().toString();
    }

    public void setEtumerkki(String etumerkki) {
        this.etumerkki.setText(etumerkki);
    }

    public Lauseke getOsoittaja() {
        return osoittaja;
    }

    public Lauseke getNimittaja() {
        return nimittaja;
    }

    public boolean vastaluku(){
        if (getEtumerkki().equals("-")){
            setEtumerkki("+");
            return true;
        }else if (getEtumerkki().equals("+")){
            setEtumerkki("-");
            return true;
        }
        return false;
    }

    public void kaantesluku(){

        kehys.removeViewAt(2);
        kehys.removeViewAt(0);

        Lauseke muistissa = nimittaja;
        nimittaja = osoittaja;
        osoittaja = muistissa;

        kehys.addView(osoittaja, 0);
        kehys.addView(nimittaja, 2);
    }

    public void luoLayout(Context context){
        otatilaa(this);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(etumerkki);

        kehys = new LinearLayout(context);
        kehys.setOrientation(LinearLayout.VERTICAL);
        otatilaa(kehys);
        this.addView(kehys);

        kehys.addView(osoittaja);
        kehys.addView(luoJakoviiva(context));
        kehys.addView(nimittaja);
        nimittaja.setOnLongClickListener(longlistener);
    }

    public LinearLayout luoJakoviiva(Context context){
        LinearLayout alue = new LinearLayout(context);

        alue.setOrientation(LinearLayout.HORIZONTAL);
        alue.setLayoutParams(new LayoutParams
                (LayoutParams.MATCH_PARENT, 30));
        alue.setGravity(Gravity.CENTER);
        LinearLayout viiva = new LinearLayout(context);
        viiva.setBackgroundColor(Color.BLACK);
        viiva.setLayoutParams(new LayoutParams
                (LayoutParams.MATCH_PARENT, 5));
        alue.addView(viiva);
        alue.setOnLongClickListener(longlistener);
        return alue;
    }

    //TODO tarpeen mukaan vaihto static
    public void otatilaa(LinearLayout alue) {
        alue.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        alue.setGravity(Gravity.CENTER);
    }

    View.OnLongClickListener longlistener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {

            Termi omistaja = (Termi) v.getParent().getParent();

            ClipData.Item item = new ClipData.Item(getPuoli());
            ClipData dragData = new ClipData(
                    "tiedot",
                    new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},item
            );

            View.DragShadowBuilder shadowbuilder = new MyDragShadowBuilder(omistaja, 7);
            v.startDrag(dragData, shadowbuilder, omistaja, 0);
            omistaja.setVisibility(View.INVISIBLE);
            return true;
        }
    };

    public static TextView luoTextView(Context context, String merkki){
        TextView etumerkki = new TextView(context);
        etumerkki.setPadding(20, 0, 20, 0);
        etumerkki.setText(merkki);
        etumerkki.setTextSize(20);
        return etumerkki;
    }

}
