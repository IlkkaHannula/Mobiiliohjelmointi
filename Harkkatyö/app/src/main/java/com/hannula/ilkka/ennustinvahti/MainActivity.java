package com.hannula.ilkka.ennustinvahti;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;


public class MainActivity extends AppCompatActivity {
    private FirebaseJobDispatcher firebaseJobDispatcher;
    private EnnusteService ennusteService;

    private EditText rajaEditText;
    private EditText valiEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        Driver driver = new GooglePlayDriver(this);
        firebaseJobDispatcher = new FirebaseJobDispatcher(driver);
        ennusteService = ennusteService;

        ToggleButton paallaButton = findViewById(R.id.paallaButton);
        rajaEditText = findViewById(R.id.RajaEditText);
        valiEditText = findViewById(R.id.ValiEditText);
        Button rajaButton = findViewById(R.id.rajaButton);
        Button valiButton = findViewById(R.id.valiButton);

        paallaButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    EnnustePreferences.asetaHalytyksetPaalle(context,true);
                    halytyksetPaalle();
                }else{
                    EnnustePreferences.asetaHalytyksetPaalle(context,false);
                    halytyksetPoisPaalta();
                }
            }
        });

        rajaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float arvo = Float.parseFloat(rajaEditText.getText().toString());
                if (arvo >= 0.5 && arvo <= 2.5){
                    EnnustePreferences.tallennaEnnusteRaja(context, arvo);
                    Toast.makeText(context, "Raja asetettu", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Arvon tulee olla välilta 0.5-2.5", Toast.LENGTH_SHORT).show();
                }
            }
        });

        valiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int arvo = Integer.parseInt(valiEditText.getText().toString());
                if (arvo >= 1 && arvo <= 72){
                    EnnustePreferences.tallennaHalytysvali(context, arvo);
                    Toast.makeText(context, "Vali asetettu", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Arvon tulee olla välilta 1-72", Toast.LENGTH_SHORT).show();
                }

            }
        });

        boolean paalla = EnnustePreferences.onkoHalytyksetPaalla(this);
        paallaButton.setChecked(paalla);
        rajaEditText.setText(String.valueOf(EnnustePreferences.haeEnnusteRaja(this)));
        valiEditText.setText(String.valueOf(EnnustePreferences.haeHalytysvali(this)));

        Toast.makeText(this, "Raja asetettu", Toast.LENGTH_SHORT).show();

        if (paalla){
            luoHalytysFirebaseJob();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("methodName").equals("bootingStart")){
                finish();
                if (paalla) {

                    ennusteService.lahetaNotifikaatio("Aaltovahti paalla, jos et tarvi kannattaa se laittaa pois", this);
                }
            }
        }
    }

    private void halytyksetPaalle(){
        luoHalytysFirebaseJob();
    }

    private void halytyksetPoisPaalta(){
        firebaseJobDispatcher.cancelAll();
    }

    private void luoHalytysFirebaseJob(){

        Job constraintReminderJob = firebaseJobDispatcher.newJobBuilder()
                .setService(EnnusteService.class)
                .setTag("firebaseJobTag")
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(7200,8100))
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(constraintReminderJob);
    }
}
