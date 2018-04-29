package com.hannula.ilkka.ennustinvahti;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;


public class MainActivity extends AppCompatActivity {
    private Button hakuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        hakuButton = findViewById(R.id.hakuButton);
        final EnnusteService asd = new EnnusteService();

        hakuButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        asd.lahetaNotifikaatio("asd", context);
                    }
                }
        );

        Driver driver = new GooglePlayDriver(this);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = firebaseJobDispatcher.newJobBuilder()
                .setService(EnnusteService.class)
                .setTag("firebaseJobTag")
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                //lopulta kannattaa laittaa tyyliin 2h ja 2h15min tms
                .setTrigger(Trigger.executionWindow(60,120))
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(constraintReminderJob);
    }
}
