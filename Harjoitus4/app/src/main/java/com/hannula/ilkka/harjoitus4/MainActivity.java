package com.hannula.ilkka.harjoitus4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private EditText[] ekatLuvut = new EditText[4];
    private EditText[] tokatLuvut = new EditText[4];
    private TextView[] tulokset = new TextView[4];

    private Button[] laskunapit = new Button[4];

    private Button tyhjenna;
    private Button nayta;

    private TextView logiTextview;

    private Vector<String> logiMerkinnat = new Vector<String>(2);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ekatLuvut[0] = (EditText) findViewById(R.id.numero1_1);
        ekatLuvut[1] = (EditText) findViewById(R.id.numero1_2);
        ekatLuvut[2] = (EditText) findViewById(R.id.numero1_3);
        ekatLuvut[3] = (EditText) findViewById(R.id.numero1_4);

        tokatLuvut[0] = (EditText) findViewById(R.id.numero2_1);
        tokatLuvut[1] = (EditText) findViewById(R.id.numero2_2);
        tokatLuvut[2] = (EditText) findViewById(R.id.numero2_3);
        tokatLuvut[3] = (EditText) findViewById(R.id.numero2_4);

        tulokset[0] = (TextView) findViewById(R.id.tulos1);
        tulokset[1] = (TextView) findViewById(R.id.tulos2);
        tulokset[2] = (TextView) findViewById(R.id.tulos3);
        tulokset[3] = (TextView) findViewById(R.id.tulos4);

        laskunapit[0] = (Button) findViewById(R.id.laskeButton1);
        laskunapit[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pluslasku();
            }
        } );

        laskunapit[1] = (Button) findViewById(R.id.laskeButton2);
        laskunapit[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                miinuslasku();
            }
        } );

        laskunapit[2] = (Button) findViewById(R.id.laskeButton3);
        laskunapit[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                kertolasku();
            }
        } );

        laskunapit[3] = (Button) findViewById(R.id.laskeButton4);
        laskunapit[3].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                jakolasku();
            }
        } );

        Button tyhjennaNappi = (Button) findViewById(R.id.tyhjennaButton);
        tyhjennaNappi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tyhjenna();
            }
        } );

        Button logiNappi = (Button) findViewById(R.id.logiButton);
        logiNappi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                naytaLogi();
            }
        } );

        logiTextview = (TextView) findViewById(R.id.logi);
    }

    public void pluslasku() {
        EditText osa1 = (EditText) findViewById(R.id.numero1_1);
        EditText osa2 = (EditText) findViewById(R.id.numero2_1);
        TextView tulosruutu = (TextView) findViewById(R.id.tulos1);
        int luku1 = Integer.parseInt(osa1.getText().toString());
        int luku2 = Integer.parseInt(osa2.getText().toString());
        int vastaus = luku1 + luku2;
        tulosruutu.setText(Integer.toString(vastaus));
        logiMerkinnat.add(osa1.getText().toString() + " + " + osa2 .getText().toString() + " = " + vastaus);
    }

    public void miinuslasku() {
        EditText osa1 = (EditText) findViewById(R.id.numero1_2);
        EditText osa2 = (EditText) findViewById(R.id.numero2_2);
        TextView tulosruutu = (TextView) findViewById(R.id.tulos2);
        int luku1 = Integer.parseInt(osa1.getText().toString());
        int luku2 = Integer.parseInt(osa2.getText().toString());
        int vastaus = luku1 - luku2;
        tulosruutu.setText(Integer.toString(vastaus));
        logiMerkinnat.add(osa1.getText().toString() + " - " + osa2 .getText().toString() + " = " + vastaus);
    }

    public void kertolasku() {
        EditText osa1 = (EditText) findViewById(R.id.numero1_3);
        EditText osa2 = (EditText) findViewById(R.id.numero2_3);
        TextView tulosruutu = (TextView) findViewById(R.id.tulos3);
        int luku1 = Integer.parseInt(osa1.getText().toString());
        int luku2 = Integer.parseInt(osa2.getText().toString());
        int vastaus = luku1 * luku2;
        tulosruutu.setText(Integer.toString(vastaus));
        logiMerkinnat.add(osa1.getText().toString() + " x " + osa2 .getText().toString() + " = " + vastaus);
    }

    public void jakolasku() {
        EditText osa1 = (EditText) findViewById(R.id.numero1_4);
        EditText osa2 = (EditText) findViewById(R.id.numero2_4);
        TextView tulosruutu = (TextView) findViewById(R.id.tulos4);
        int luku1 = Integer.parseInt(osa1.getText().toString());
        int luku2 = Integer.parseInt(osa2.getText().toString());
        int vastaus = luku1 / luku2;
        tulosruutu.setText(Integer.toString(vastaus));
        logiMerkinnat.add(osa1.getText().toString() + " / " + osa2 .getText().toString() + " = " + vastaus);
        String teksti = osa1.getText().toString() + " / " + osa2 .getText().toString() + " = " + vastaus;
        Log.d("myTag", teksti);
    }

    public void tyhjenna() {
        for(int i=0; i<4; i++){
            ekatLuvut[i].setText("");
            tokatLuvut[i].setText("");
            tulokset[i].setText("");
        }
    }

    public void naytaLogi() {
        String teksti = "";
        for(int i=0; i<5; i++){
            int viimeinen = logiMerkinnat.size();
            teksti += logiMerkinnat.get(viimeinen-i);
        }
        logiTextview.setText("");
    }

}
