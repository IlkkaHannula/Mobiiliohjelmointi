package com.hannula.ilkka.yhtalo;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText osoittaja, nimittaja;
    private Yhtalonpuoli vasenPuoli, oikeaPuoli;
    private boolean lisaaVasemmalle;
    private String etumerkki;
    private int lightGray = R.color.lightGray;
    private GridLayout valintaGrid;
    private LinearLayout vasen, oikea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alustus();
        //TODO joskus kun kaantyminen toimii niin kaannoksen jalkeen zoom menee 3.0 tai jotain

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.syote_valikko, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nappisEsiin) {
            //TODO custom keyboard?
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(missaFocus(), InputMethodManager.SHOW_FORCED);
        }

        return super.onOptionsItemSelected(item);
    }

    public void alustus(){
        osoittaja = (EditText) findViewById(R.id.osoittaja);
        nimittaja = (EditText) findViewById(R.id.nimittaja);

        osoittaja.setShowSoftInputOnFocus(false);
        nimittaja.setShowSoftInputOnFocus(false);

        vasen = ((LinearLayout) findViewById(R.id.vasenpuoli));
        vasen.setOnDragListener(dragListener);

        oikea = ((LinearLayout) findViewById(R.id.oikeapuoli));
        oikea.setOnDragListener(dragListener);

        vasenPuoli = new Yhtalonpuoli(vasen, "vasen");
        oikeaPuoli = new Yhtalonpuoli(oikea, "oikea");


        lisaaVasemmalle = true;
        etumerkki = "+";

        valintaGrid = (GridLayout) findViewById(R.id.merkit);
        for (int i = 1; i < 5; i++) {
            ((TextView) valintaGrid.getChildAt(i)).setTextColor(lightGray);
        }
    }


    View.OnDragListener dragListener = new View.OnDragListener() {
    @Override
    public boolean onDrag(View v, DragEvent event) {

        View view = (View) event.getLocalState();
        //LinearLayout owner = (LinearLayout) view.getParent();

        Termi termi = ((Termi) view);

        int dragEvent = event.getAction();
        switch (dragEvent) {


            case DragEvent.ACTION_DRAG_STARTED:



                return true;

            case DragEvent.ACTION_DRAG_ENTERED:

                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                return true;

            case DragEvent.ACTION_DRAG_ENDED:



                /*if(event.getResult()){

                    Toast.makeText(MainActivity.this,"The drop was handled.",Toast.LENGTH_SHORT).show();
                }*/

                view.setVisibility(View.VISIBLE);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:

                return true;

            case DragEvent.ACTION_DROP:

                String lahtoPuoli = event.getClipData().getItemAt(0).getText().toString();

                boolean vasemmalta = false;
                boolean oikealta = false;

                //Toast.makeText(MainActivity.this, dragData, Toast.LENGTH_SHORT).show();
                if (lahtoPuoli.equals("vasen") && v == oikea){
                    vasemmalta = true;
                }else  if (lahtoPuoli.equals("oikea") && v == vasen){
                    oikealta = true;
                }
                if (termi.getEtumerkki().equals("+")|| termi.getEtumerkki().equals("-")){
                    termi.kaantesluku();
                }

                if (vasemmalta) {
                    vasen.removeView(view);
                    termi.vastaluku();
                    oikea.addView(view);
                }else if (oikealta) {
                    oikea.removeView(view);
                    termi.vastaluku();
                    vasen.addView(view);
                }
                //siirrä
                return true;

        }
        return false;
    }
};

    public void puolenValinta(View view){
        if (((TextView) view).getText().toString().equals("vasenpuoli")) {
            lisaaVasemmalle = true;
            ((TextView) valintaGrid.getChildAt(4)).setTextColor(lightGray);
        } else {
            lisaaVasemmalle = false;
            ((TextView) valintaGrid.getChildAt(5)).setTextColor(lightGray);
        }
        ((TextView) view).setTextColor(Color.BLACK);
    }


    public void etumerkinValinta(View view) {
        etumerkki = ((TextView) view).getText().toString();

        for (int i = 0; i < 4; i++) {
            ((TextView) valintaGrid.getChildAt(i)).setTextColor(lightGray);
        }

        ((TextView) view).setTextColor(Color.BLACK);
    }


    public void luoTermi(View view) {

        Termi uusi = new Termi(etumerkki, osoittaja.getText().toString(),
                nimittaja.getText().toString(), this);

        uusi.kaantesluku();

        if (lisaaVasemmalle) {
            vasenPuoli.lisaaTermi(uusi);
        }else{
            oikeaPuoli.lisaaTermi(uusi);
        }
    }

    public void lukitse(View view){
        //TODO vaihda tila
    }

    //TODO siirra nappaimistomoduuliin?

    public void lisaaMerkki(View view){
        String merkki = ((TextView) view).getText().toString();
        EditText editText = missaFocus();

        int kohta = editText.getSelectionStart();
        String sisalto = editText.getText().toString();
        String teksti = sisalto.substring(0,kohta) + merkki + sisalto.substring(kohta);
        editText.setText(teksti);
        editText.setSelection(kohta +1);
    }

    public EditText missaFocus(){
        if (osoittaja.hasFocus()){
            return osoittaja;
        }else{
            return nimittaja;
        }
    }

    public void liikuVasemmalle(View view){
        liikutaKursoria(-1);
    }

    public void liikuOikealle(View view){
        liikutaKursoria(1);
    }

   public void liikutaKursoria(int suunta){
        EditText editText = missaFocus();
        int kohta = editText.getSelectionStart() + suunta;
        if (0 <= kohta && kohta <=  editText.getText().length()){
            missaFocus().setSelection(kohta);
        }
    }


    public void poistaMerkki(View view){
        EditText editText = missaFocus();
        int kohta = editText.getSelectionStart();
        if (editText.getText().length() > 0 && kohta > 0){
            String sisalto = editText.getText().toString();
            String teksti = sisalto.substring(0, kohta - 1)+ sisalto.substring(kohta);
            editText.setText(teksti);
            editText.setSelection(kohta - 1);
        }
    }
}
