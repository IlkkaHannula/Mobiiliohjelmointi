/*Ilkka Hannula 21.2.2018 vanhan Tilinumero muistirjan pohjalta. (20.2.2016)
Tassa sovelluksessa voidaan pitää kirjaa kavereiden merkkipaivista.

Tietoja sovelluksessa voi lisata, muokata, poistaa, etsia ja puhelinnumerosta saa leikepoydalle kopion.
Lisaaminen tapahtuu helposti napeilla. Kopiointi toimii painamalla normaalitilassa pitkaan haluttua
tietoalkiota. Myos muokkaus ja poista tehdaan niin, etta valitaan ensin oikea tila ja sitten painetaan
haluttua alkiota pitkaan kunnes sen tekstit ponnahtaa muokattaviksi tai tulee varmennus, etta halutaan
poistaa. Etsi loytyy menusta ja se hakee nimen tai sen alkun perusteella ja alkion loytyessa scrollaa
kohdalle. Kaytossa on myos ylos ja alas scrollaus selaamisen helpottamiseksi.
 */


package com.hannula.ilkka.muistio;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Muistio extends AppCompatActivity {
    EditText nimi_teksti,numero_teksti, synttari_teksti, nimppari_teksti;
    Button lisaa_nappi, muokkaa_nappi, toteuta_muokkaus_nappi, lopeta_poisto_nappi;
    ScrollView scrollattava_alue;
    TextView scrollaus_teksti;
    boolean muokkaus, poisto;
    InputMethodManager imm;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ValueEventListener mValueEventListener;
    private List<Info> infot;

    static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("infot");
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_muistio);
        nimi_teksti = (EditText)findViewById(R.id.nimi_syote);
        numero_teksti = (EditText)findViewById(R.id.numero_syote);
        synttari_teksti = (EditText)findViewById(R.id.syntymapaiva_syote);
        nimppari_teksti = (EditText)findViewById(R.id.nimipaiva_syote);
        lisaa_nappi = (Button)findViewById(R.id.lisaa_nappi);
        muokkaa_nappi = (Button)findViewById(R.id.muokkaa_nappi);
        toteuta_muokkaus_nappi = (Button) findViewById(R.id.toteuta_muokkaus_nappi);
        lopeta_poisto_nappi = (Button) findViewById(R.id.lopeta_poisto_nappi);
        scrollattava_alue = (ScrollView) findViewById(R.id.scrollattava_alue);
        muokkaus = false;
        poisto = false;
        infot = new ArrayList<Info>();

        nappien_alustus();
        alusta_tekstit();

        imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);

         mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    onSignedInInitializer(user.getDisplayName());
                }
                else{
                    onSignedOutCleanup();
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
                }
            }
        };

        //lisaa_tarkistus_dataa();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
        if (mValueEventListener != null){
            databaseReference.removeEventListener(mValueEventListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_muistio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.etsi) {
            etsi_haluttu();
            return true;
        }
        else if (id == R.id.poista) {
            poista_kayttoon();
            muokkaus_pois();
            return true;
        }
        else if (id == R.id.scrollaa_alas) {
            scrollattava_alue.fullScroll(ScrollView.FOCUS_DOWN);
            return true;
        }
        else if (id == R.id.kirjaa_ulos) {
            AuthUI.getInstance().signOut(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void onSignedInInitializer(String displayName){
        if (mValueEventListener == null){
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    infot = new ArrayList<Info>();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Info info = singleSnapshot.getValue(Info.class);

                        if (info != null){
                            infot.add(info);
                        }
                    }
                    nayta_tallennetut();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(Muistio.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            };
            databaseReference.addValueEventListener(mValueEventListener);
        }
    }

    private void onSignedOutCleanup(){
        scrollattava_alue.removeAllViews();
        if (mValueEventListener != null){
            databaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    //sisaltaa nappien onclicklisterien alustuksen
    public void nappien_alustus(){
        lisaa_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //otetaan syotteet ylos ja tarkistetaan
                        String nimi = nimi_teksti.getText().toString().toUpperCase();
                        String numero = numero_teksti.getText().toString();
                        String synttari = synttari_teksti.getText().toString();
                        String nimppari = nimppari_teksti.getText().toString();

                        if (!tarkista_syotteet(nimi, numero, synttari, nimppari,"", "")) {
                            return;
                        }

                        //lisataan tiedot tietokantaan ja nakyville,  scrollataan kohdalle
                        Info info = new Info(nimi, numero, synttari, nimppari);
                        databaseReference.child(nimi.toUpperCase()).setValue(info);

                        Toast.makeText(Muistio.this, "Henkilön " + nimi.substring(0, 1) +
                                nimi.substring(1).toLowerCase() + " tiedot lisätty", Toast.LENGTH_SHORT).show();
                        alusta_tekstit();
                        scrollaa_alkioon(nimi);
                        //otetaan viela nappaimisto alas
                        imm.hideSoftInputFromWindow(nimi_teksti.getWindowToken(), 0);

                    }
                }
        );

        muokkaa_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!muokkaus) {
                            muokkaus_paalle();
                        }
                        else {
                            muokkaus_pois();
                            Toast.makeText(Muistio.this, "Poistuttu muokkauksesta", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
        );

        lopeta_poisto_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lopeta_poisto();
                    }
                });
    }

    //tama onclicklistener huolehtii siita, etta oikealle tiedolle tehdaan muokkaus
    public void valmistele_muokkaus(final Info info){
        nimi_teksti.setText(info.getNimi());
        numero_teksti.setText(info.getNumero());
        synttari_teksti.setText(info.getSynttari());
        nimppari_teksti.setText(info.getNimppari());


        toteuta_muokkaus_nappi.setEnabled(true);
        toteuta_muokkaus_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override

                    //alustetaan siis muokkaus napin onclicklisteri oikeille tiedoille
                    public void onClick(View v) {
                        muokkaa(info);
                        imm.hideSoftInputFromWindow(nimi_teksti.getWindowToken(), 0);
                    }
                }
        );
    }

    //muokkaamattomat parametrit anneteaan niita tapauksia varten, joissa muokataan vanhaa
    //tietoa, eli silloin muokkaamaton nimi/numero sallitaan paallekkaisyyksissa
    public boolean tarkista_syotteet(String nimi, String numero, String synttari_pvm, String nimppari_pvm,
                                     String muokkaamaton_nimi, String muokkaamaton_numero){

        if (nimi.equals("")) {
            Toast.makeText(Muistio.this, "Lisää nimi", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!nimi.matches("[a-ö A-Ö]+")){
            Toast.makeText(Muistio.this, "Nimessä ei sallittuja merkkejä", Toast.LENGTH_SHORT).show();
            return false;
        }

        //valilyontien maara rajattu yhtee, jotta valtytaan extra valilyonnin kanssa samoilta nimilta
        if (laske_maara(nimi, ' ') > 1){
            Toast.makeText(Muistio.this, "Nimessä liikaa välilyöntejä", Toast.LENGTH_SHORT).show();
            return false;
        }


        for(Info info: infot) {
            if (info.getNimi().equals(nimi.toUpperCase()) && !info.getNimi().equals(muokkaamaton_nimi)) {
                Toast.makeText(Muistio.this, "Nimelle on jo tallennettu tietoja", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (laske_maara(synttari_pvm, '.') != 2){
            Toast.makeText(Muistio.this, "Syntymäpäivä väärää muotoa", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (laske_maara(nimppari_pvm, '.') != 2){
            Toast.makeText(Muistio.this, "Nimipäivä väärää muotoa", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public int laske_maara(String text, char merkki){
        int maara = 0;
        for( int i=0; i<text.length(); i++ ) {
            if( text.charAt(i) == merkki ) {
                maara++;
            }
        }
        return maara;
    }

    public void nayta_tallennetut(){

        LinearLayout tekstit;

        //jos tietokanta on viela tyhja riittaa tyhja layout
        if (infot.size() == 0){
            tekstit = new LinearLayout(this);
            tekstit.setOrientation(LinearLayout.VERTICAL);

        }//muissatapauksissa tehdaan layout missa on kaikki data clickattavana
        else{
            tekstit = lisaa_tiedot(infot);
            muokkaa_nappi.setEnabled(true);
        }

        //tehdaan viela yksi alkio ylos scrollausta varten
        scrollaus_teksti = new TextView(this);
        scrollaus_teksti.setTextSize(9 * getResources().getDisplayMetrics().density);
        scrollaus_teksti.setText("\n" + "Tästä painamalla scrollaa takaisin ylös");
        scrollaus_teksti.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scrollattava_alue.scrollTo(0, 0);
            }
        });
        tekstit.addView(scrollaus_teksti);
        if (tekstit.getChildCount() == 1){
            scrollaus_teksti.setVisibility(View.GONE);
        }

        scrollattava_alue.removeAllViews();
        scrollattava_alue.addView(tekstit);

    }

    public LinearLayout lisaa_tiedot(List<Info> infot) {
        //tehdaan ensin pystyssa oleva lineaarinen layout, johon textviewit voi lisata
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //tehdaan jokaisesta alkiosta omat textviewit ja laitetaan ne layouttiin
       for (Info info: infot){
            TextView tietoalkio = tee_tieto_alkio(info);
            layout.addView(tietoalkio);
        }
        return layout;
    }

    public TextView tee_tieto_alkio(final Info info){
        String nimi = info.getNimi();

        //tarkastetaan loytyyko valilyontija ja ja kirjoitetaan nimet jarkevampaan muotoon
        int valin_paikka = nimi.indexOf(" ");
        final String naytettava_nimi;

        if (valin_paikka == -1){
            naytettava_nimi = nimi.substring(0,1) + nimi.substring(1).toLowerCase();
        }
        else{
            naytettava_nimi = nimi.substring(0,1) + nimi.substring(1,valin_paikka+1).toLowerCase()
                    +nimi.substring(valin_paikka+1,valin_paikka+2)+ nimi.substring(valin_paikka+2).toLowerCase();
        }

        //tehdaan textview jossa on onclick listener kopioinnin, muokkauksen ja poiston takia
        TextView tietoalkio = new TextView(this);
        tietoalkio.setTextSize(9 * getResources().getDisplayMetrics().density);
        tietoalkio.setText(naytettava_nimi + "\n" + "Puh: " + info.getNumero() + "\n" + info.getSynttari() + "   \n" + info.getNimppari() + "\n");
        tietoalkio.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if (!muokkaus && !poisto) {
                    //kopioidaan painetun henkilon pugelinnumero leikepoydalle
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("kopioitu numero", info.getNumero());
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(v.getContext(), "Henkilön " + naytettava_nimi + " Puhelinnumero kopioitu", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (muokkaus) {
                    valmistele_muokkaus(info);
                    return true;
                }
                else {
                    kysy_varmistus_poistolle(info, "Varoitus!", "Haluatko varmasti poistaa henkilön " + naytettava_nimi + " tiedot");
                    return true;
                }
            }
        });
        return tietoalkio;
    }

    public void kysy_varmistus_poistolle(final Info info, String title, String message){
        //luodaan popupikkuna johon tulee napit poiston varmistamiselle
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.kylla, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //jos vastaus on myontava poistetaan aiemmin klikattu alkio

                databaseReference.child(info.getNimi()).removeValue();
                infot.remove(info);

                Toast.makeText(Muistio.this, "Henkilön " + info.getNimi() + " tiedot poistettu", Toast.LENGTH_SHORT).show();

                if (onko_tyhja()) {
                    lopeta_poisto();
                    muokkaa_nappi.setEnabled(false);
                    scrollaus_teksti.setVisibility(View.GONE);
                }
            }
        });

        builder.setNegativeButton(R.string.peruuta, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void muokkaa(Info info){
        String nimi = info.getNimi();
        String numero = info.getNumero();
        String synttari = info.getSynttari();
        String nimppari = info.getNimppari();

        //otetaan ylos muutettavat tiedot ja muokataan dataa
        String uusi_nimi = nimi_teksti.getText().toString().toUpperCase();
        String uusi_numero = numero_teksti.getText().toString().toUpperCase();
        String uusi_synttari = synttari_teksti.getText().toString().toUpperCase();
        String uusi_nimppari = nimppari_teksti.getText().toString().toUpperCase();

        //jos mitaan ei muokattu ei reagoidakkaan
        if (uusi_nimi.equals(nimi.toUpperCase()) && uusi_numero.equals(numero) &&
                uusi_synttari.equals(synttari) && uusi_nimppari.equals(nimppari)){
            Toast.makeText(Muistio.this, "Mitään ei muutettu", Toast.LENGTH_SHORT).show();
            muokkaus_pois();
            return;
        }
        //jos tiedot ei kelpaa annetaan kayttajan yrittaa uudestaan
        if (!tarkista_syotteet(uusi_nimi, uusi_numero, synttari, nimppari, nimi.toUpperCase(), numero)) {
            return;
        }

        databaseReference.child(info.getNimi()).child("numero").setValue(uusi_numero);
        databaseReference.child(info.getNimi()).child("synttari").setValue(uusi_synttari);
        databaseReference.child(info.getNimi()).child("nimppari").setValue(uusi_nimppari);
        //databaseReference.child(info.getNimi()).child("nimi").setValue(uusi_nimi);


        Toast.makeText(Muistio.this, "Henkilön " + nimi + " tiedot muokattu", Toast.LENGTH_SHORT).show();

        //tuskin kayttajan tarvitsee montaa muokata, joten muokkauksesta pois vahinkomuokkausten valttamiseksi
        muokkaus_pois();
        scrollaa_alkioon(uusi_nimi);
    }

    //tehdaan uusi alkio, etsitaan sen paikka ja lisataan se sinne
    public void lisaa_alkio(Info info, String vanha_nimi){
        TextView lisattava = tee_tieto_alkio(info);
        int lisays_indeksi = loyda_lisattava_indeksi(info.getNimi().toUpperCase(), vanha_nimi);
        if (lisays_indeksi == 0){
            muokkaa_nappi.setEnabled(true);
            scrollaus_teksti.setVisibility(View.VISIBLE);
        }
        ((LinearLayout) scrollattava_alue.getChildAt(0)).addView(lisattava, lisays_indeksi);
    }

    //vastaavasti poistaessa etsitaan ensin poistettava paikka
    public void poista(String nimi){
        int indeksi = loyda_alkion_indeksi(nimi);
        ((LinearLayout)scrollattava_alue.getChildAt(0)).removeViewAt(indeksi);

    }

    //nappien yms saatelya
    public void muokkaus_paalle(){
        muokkaus = true;
        muokkaa_nappi.setText(R.string.muokkaus_pois);
        lisaa_nappi.setVisibility(View.GONE);
        toteuta_muokkaus_nappi.setVisibility(View.VISIBLE);
        toteuta_muokkaus_nappi.setEnabled(false);
        Toast.makeText(Muistio.this, "Muokkaus käytössä", Toast.LENGTH_SHORT).show();
        nimi_teksti.setEnabled(false);
    }

    public void muokkaus_pois() {
        muokkaus = false;
        muokkaa_nappi.setText(R.string.muokkaus_paalle);
        lisaa_nappi.setVisibility(View.VISIBLE);
        toteuta_muokkaus_nappi.setVisibility(View.GONE);
        alusta_tekstit();
        nimi_teksti.setEnabled(false);
    }

    public void poista_kayttoon(){
        if (onko_tyhja()){
            Toast.makeText(Muistio.this, "Ei poistettavia tietoja", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(Muistio.this, "Poista haluttu painamalla pitkään", Toast.LENGTH_SHORT).show();
        lopeta_poisto_nappi.setVisibility(View.VISIBLE);
        muokkaa_nappi.setVisibility(View.GONE);
        lisaa_nappi.setVisibility(View.GONE);
        nimi_teksti.setEnabled(false);
        numero_teksti.setEnabled(false);
        synttari_teksti.setEnabled(false);
        nimppari_teksti.setEnabled(false);
        poisto = true;
    }

    public void lopeta_poisto(){
        lopeta_poisto_nappi.setVisibility(View.GONE);
        muokkaa_nappi.setVisibility(View.VISIBLE);
        lisaa_nappi.setVisibility(View.VISIBLE);
        nimi_teksti.setEnabled(true);
        numero_teksti.setEnabled(true);
        synttari_teksti.setEnabled(true);
        nimppari_teksti.setEnabled(true);
        poisto = false;
    }

    public void etsi_haluttu(){
        if (onko_tyhja()){
            Toast.makeText(Muistio.this, "Tietokannassa ei ole vielä tietoja", Toast.LENGTH_SHORT).show();
            return;
        }

        //etsiessa luodaan taas popup ikkuna joka kysyy hakuavainta
        final EditText edittext = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Kirjoita nimi tai alkua");
        builder.setView(edittext);
        builder.setPositiveButton(R.string.hae, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scrollaa_haettuun(edittext.getText().toString());
            }
        });

        builder.setNegativeButton(R.string.peruuta, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = builder.create();

        //helppokayttoisyyden vuoksi lisataan siihen viela tunnistus entterille
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {
                    edittext.requestFocus();
                    scrollaa_haettuun(edittext.getText().toString());
                    imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

    //etsiaan indeksi ja scrollataan haetun alkion kohdalle
    public void scrollaa_haettuun(String nimi){
        int indeksi = loyda_haettava_indeksi(nimi);
        if (indeksi == -1 ){
            Toast.makeText(Muistio.this, "Ei löytynyt vastaavia", Toast.LENGTH_SHORT).show();
            return;
        }
        int korkeus = ((LinearLayout) scrollattava_alue.getChildAt(0)).getChildAt(0).getHeight();
        scrollattava_alue.scrollTo(0, (indeksi) * korkeus);
    }

    //eri versio siirtymisia varten, ettei vain jaisi Annamaria lisatessa Annan kohdalle
    public void scrollaa_alkioon(String nimi){
        int indeksi = loyda_alkion_indeksi(nimi);
        int korkeus = ((LinearLayout) scrollattava_alue.getChildAt(0)).getChildAt(0).getHeight();
        scrollattava_alue.scrollTo(0, (indeksi) * korkeus);
    }

    public int loyda_haettava_indeksi(String nimi){
        int indeksi =0;
        boolean loytyi = false;

        //kaydaan data lapi ja pysahdytaan kun jonkun nimen alkuosa vastaa hakuavainta
        for (Info info: infot) {
            String nimi_tieto = info.getNimi();
            if (nimi_tieto.length() >= nimi.length()){
                if (nimi_tieto.substring(0,nimi.length()).equals(nimi.toUpperCase())){
                    loytyi = true;
                    break;
                }
            }
            indeksi++;
        }
        if (!loytyi){
            return -1;
        }
        return indeksi;
    }

    //funktio joka etsii sen valin mihin uusi alkio halutaan lisata
    public int loyda_lisattava_indeksi(String nimi, String vanha_nimi){
        int indeksi = 0 ;

        //kaydaan dataa lapi kunnes loytyy aakkosittain myohempana oleva nimi
        for (Info info: infot) {
            String nimi_tieto = info.getNimi();

            //ettei muokkauksissa edellisen nimen yli mentaessa tulisi indeksin lisaysta
            //joska tama edellinen tietohan muokkaantuu ja siirretaa eli poistuu sitten alta
            if (nimi_tieto.equals(vanha_nimi)){
                continue;
            }
            else if (nimi_tieto.compareTo(nimi) > 0){
                return indeksi;
            }
            indeksi++;
        }
        return indeksi;
    }

    //kaydaan dataa taas niin kauan, etta haluttu alkio loytyy
    public int loyda_alkion_indeksi(String nimi){
        int indeksi = 0;
        for (Info info: infot) {
            String nimi_tieto = info.getNimi();
            if (nimi_tieto.equals(nimi.toUpperCase())){
                break;
            }
            indeksi++;
        }
        return indeksi;
    }

    //apufunktioita selkeyttamaan koodia
    public boolean onko_tyhja(){
        return infot.size() == 0;
    }

    public void alusta_tekstit(){
        nimi_teksti.setText("");
        numero_teksti.setText("");
        synttari_teksti.setText("");
        nimppari_teksti.setText("");
    }

    //tama funktion luo validia dataa testauksen helpottamista varten
    public void lisaa_tarkistus_dataa(){
        String[] nimet = {"Aino","Matti","Jukka","Kari","Ville","Pekka","Riku","Joona","Suvi","Jarno","Kalle",
        "Akseli","Teemu","Jari","Olli","Raimo","Anna","Aatu","Laura","Ritva","Tuija","Kaarlo","Johanna","Mari"};
        int j = 1;
        for (String nimi: nimet){
            String numero = "0456747" + j;
            String synttarit = "01." + String.valueOf(j) + ".1993";
            String nimpparit = "05." + String.valueOf(j) +".";

            Info info = new Info(nimi.toUpperCase(),  numero, synttarit, nimpparit);

            databaseReference.child(nimi.toUpperCase()).setValue(info);

            j++;
        }
    }
}
