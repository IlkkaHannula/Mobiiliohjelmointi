/*Ilkka Hannula 20.2.2016 Tilinumero muistirja
Tassa sovelluksessa voidaan pitää kirjaa kavereiden tilinumeroista. Tilinumerot tallennetaan
nimelle, jonka pitää sisaltaa kirjaimia ja se saa sisaltaa yhden valilyonnin (toisille nimille
tuskin loytyy tarvetta). Tilinumeron taas pitaa olla suomalaista IBAN-muotoa.

Tilinumeroita sovelluksessa voi lisata, muokata, poistaa, etsia ja ottaa leikepoydalle kopion.
Lisaaminen tapahtuu helposti napeilla. Kopiointi toimii painamalla normaalitilassa pitkaan haluttua
tietoalkiota. Myos muokkaus ja poista tehdaan niin, etta valitaan ensin oikea tila ja sitten painetaan
haluttua alkiota pitkaan kunnes sen tekstit ponnahtaa muokattaviksi tai tulee varmennus, etta halutaan
poistaa. Etsi loytyy menusta ja se hakee nimen tai sen alkun perusteella ja alkion loytyessa scrollaa
kohdalle. Kaytossa on myos ylos ja alas scrollaus selaamisen helpottamiseksi.
 */


package com.hannula.ilkka.tilinumero;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
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
import java.math.BigInteger;

public class Muistio extends AppCompatActivity {
    DatabaseHelper tietokanta;
    EditText nimi_teksti,numero_teksti, synttari_teksti, nimppari_teksti;
    Button lisaa_nappi, muokkaa_nappi, toteuta_muokkaus_nappi, lopeta_poisto_nappi;
    ScrollView scrollattava_alue;
    TextView scrollaus_teksti;
    boolean muokkaus, poisto;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muistio);
        tietokanta = new DatabaseHelper(this);
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
        nappien_alustus();
        nayta_tallennetut();
        alusta_tekstit();

        imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);

        //talla voi lisata tietoja, jolloin tarkastelu on helpompaa. huomiona kuitenkin, etta tulee
        //todella paljon toasteja, joten kannattaa avata tuon kanssa ja heti peraan uudestaan ilman
        //lisaa_tarkistus_dataa();
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
        }

        return super.onOptionsItemSelected(item);
    }

    //sisaltaa nappien onclicklisterien alustuksen
    public void nappien_alustus(){
        lisaa_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //otetaan syotteet ylos ja tarkistetaan
                        String nimi = nimi_teksti.getText().toString().toUpperCase();
                        String numero = numero_teksti.getText().toString().toUpperCase();
                        if (!tarkista_syotteet(nimi, numero, "", "")) {
                            return;
                        }

                        //lisataan tiedot tietokantaan ja nakyville,  scrollataan kohdalle
                        lisaa_alkio(nimi, numero, "");
                        tietokanta.insertData(nimi, numero);
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

    //TODO tahan mukaan muutkin kohdat
    //tama onclicklistener huolehtii siita, etta oikealle tiedolle tehdaan muokkaus
    public void valmistele_muokkaus(final String nimi, final String numero){
        nimi_teksti.setText(nimi);
        numero_teksti.setText(numero);


        toteuta_muokkaus_nappi.setEnabled(true);
        toteuta_muokkaus_nappi.setOnClickListener(
                new View.OnClickListener() {
                    @Override

                    //alustetaan siis muokkaus napin onclicklisteri oikeille tiedoille
                    public void onClick(View v) {
                        muokkaa(nimi, numero);
                        imm.hideSoftInputFromWindow(nimi_teksti.getWindowToken(), 0);
                    }
                }
        );
    }

    //muokkaamattomat parametrit anneteaan niita tapauksia varten, joissa muokataan vanhaa
    //tietoa, eli silloin muokkaamaton nimi/numero sallitaan paallekkaisyyksissa
    public boolean tarkista_syotteet(String nimi,String tilinumero,
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
        int maara = 0;
        for( int i=0; i<nimi.length(); i++ ) {
            if( nimi.charAt(i) == ' ' ) {
                if (maara == 1){
                    Toast.makeText(Muistio.this, "Nimessä liikaa välilyöntejä", Toast.LENGTH_SHORT).show();
                    return false;
                }
                maara++;
            }
        }
        Cursor data = tietokanta.getAllData();
        while (data.moveToNext()) {
            if (data.getString(1).equals(nimi.toUpperCase()) && !data.getString(1).equals(muokkaamaton_nimi)) {
                Toast.makeText(Muistio.this, "Nimelle on jo tallennettu tietoja", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if (data.getString(2).equals(tilinumero.toUpperCase()) && !data.getString(2).equals(muokkaamaton_numero)) {
                Toast.makeText(Muistio.this, "Puhelinnumero on jo tallennettu toiselle nimelle", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        /* Tarvisko jotain puh tai paivamaaria tarkistella?
        if (!tarkista_tilinumero(tilinumero)) {
            Toast.makeText(Muistio.this, "Tilinumero ei ole suomalaista IBAN-muotoa", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        return true;
    }

    public boolean tarkista_tilinumero(String tilinumero){
        //poistetaan aluksi kaikki valilyonnit, jotta tilinumeron voi antaa useassa muodossa
        String numero = "";
        for (int i = 0; i < tilinumero.length(); i++){
            if (tilinumero.charAt(i) != ' '){
                numero += tilinumero.charAt(i);
            }
        }
        //sitten tarkastetaan, etta tilinumero on oikeaa muotoa
        if (!numero.substring(0,2).equals("FI")){
            return false;
        }if (numero.length() != 18){
            return false;
        }
        //tehdaan numerosta muutosluku sen tarkistamista varten, siirretaan siis maatunnus ja kaksi
        //seuraavaa numeroa peraan ja muutetaan viela maatunnus numeroiksi
        String muutosluku = numero.substring(4) + "1518" + numero.substring(2,4);
        BigInteger tarkisteluku;
        try {
            tarkisteluku = new BigInteger(muutosluku);
        }
        catch(NumberFormatException e) {
            return false;
        }

        return (tarkisteluku.mod(new BigInteger("97")).intValue() == 1);
    }

    public void nayta_tallennetut(){

        Cursor data = tietokanta.getAllData();
        LinearLayout tekstit;

        //jos tietokanta on viela tyhja riittaa tyhja layout
        if (data.getCount() == 0){
            tekstit = new LinearLayout(this);
            tekstit.setOrientation(LinearLayout.VERTICAL);

        }//muissatapauksissa tehdaan layout missa on kaikki data clickattavana
        else{
            tekstit = lisaa_tiedot(data);
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

        scrollattava_alue.addView(tekstit);

    }

    public LinearLayout lisaa_tiedot(Cursor data) {
        //tehdaan ensin pystyssa oleva lineaarinen layout, johon textviewit voi lisata
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //TODO TÄSSÄ HAETAAN TIETOKANNASTA
        //tehdaan jokaisesta alkiosta omat textviewit ja laitetaan ne layouttiin
        while (data.moveToNext()){
            String nimi = data.getString(1);
            final String tilinumero = data.getString(2);

            TextView tietoalkio = tee_tieto_alkio(nimi, tilinumero);
            layout.addView(tietoalkio);
        }
        return layout;
    }

    public TextView tee_tieto_alkio(String nimi, final String numero){

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
        tietoalkio.setText("Nimi: " + naytettava_nimi + "\n" + "Puhelinnumero: " + numero + "\n");
        tietoalkio.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if (!muokkaus && !poisto) {
                    //kopioidaan painetun henkilon tilinumero leikepoydalle
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("kopioitu numero", numero);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(v.getContext(), "Henkilön " + naytettava_nimi + " Puhelinnumero kopioitu", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (muokkaus) {
                    valmistele_muokkaus(naytettava_nimi, numero);
                    return true;
                }
                else {
                    kysy_varmistus_poistolle(naytettava_nimi, "Varoitus!", "Haluatko varmasti poistaa henkilön " + naytettava_nimi + " tiedot");
                    return true;
                }
            }
        });
        return tietoalkio;
    }

    public void kysy_varmistus_poistolle(final String nimi, String title, String message){
        //luodaan popupikkuna johon tulee napit poiston varmistamiselle
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.kylla, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //jos vastaus on myontava poistetaan aiemmin klikattu alkio
                poista(nimi.toUpperCase());
                String id = etsi_id(nimi.toUpperCase());
                tietokanta.deleteData(id);
                Toast.makeText(Muistio.this, "Henkilön " + nimi + " tiedot poistettu", Toast.LENGTH_SHORT).show();

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

    //tata kutsuessa nimi on annettu alunperin datasta, niin se myos loytyy nyt sielta, ei tarvitse
    //siis huolehtia poikkeuksista. Kaydaan data lapi ja palautetaan nimen id
    public String etsi_id(String nimi){
        Cursor data = tietokanta.getAllData();
        String id = null;

        while (data.moveToNext()) {
            if (data.getString(1).equals(nimi)) {
                id = data.getString(0);
                break;
            }
        }
        return id;
    }

    //muokkaa saa parametreinaan vanhat tiedot
    public void muokkaa(String nimi, String numero){
        String id = etsi_id(nimi.toUpperCase());

        //otetaan ylos muutettavat tiedot ja muokataan dataa
        String uusi_nimi = nimi_teksti.getText().toString().toUpperCase();
        String uusi_numero = numero_teksti.getText().toString().toUpperCase();
        //TODO TÄHÄN UUDET TIEDOT

        //jos mitaan ei muokattu ei reagoidakkaan //TODO JA TAHAN
        if (uusi_nimi.equals(nimi.toUpperCase()) && uusi_numero.equals(numero)){
            Toast.makeText(Muistio.this, "Mitään ei muutettu", Toast.LENGTH_SHORT).show();
            muokkaus_pois();
            return;
        }
        //jos tiedot ei kelpaa annetaan kayttajan yrittaa uudestaan
        if (!tarkista_syotteet(uusi_nimi, uusi_numero, nimi.toUpperCase(), numero)) {
            return;
        }
        //kutsutaan metodia toteuttamaan muokkaus
        toteuta_muokkaus(id, nimi.toUpperCase(), uusi_nimi, uusi_numero);
        Toast.makeText(Muistio.this, "Henkilön " + nimi + " tiedot muokattu", Toast.LENGTH_SHORT).show();

        //tuskin kayttajan tarvitsee montaa muokata, joten muokkauksesta pois vahinkomuokkausten valttamiseksi
        muokkaus_pois();
        scrollaa_alkioon(uusi_nimi);
    }

    //tehdaan siirto ensin nakyvissa ja sen jalkeen kulissien takana, nain indeksit ei sotkeudu kun
    //tietokantaa kuitenkin kaytetaan jarjestelyjen laskemiseen
    public void toteuta_muokkaus(String id, String nimi, String uusi_nimi, String uusi_numero){
        poista(nimi.toUpperCase());
        lisaa_alkio(uusi_nimi, uusi_numero, nimi);
        tietokanta.updateData(id, uusi_nimi, uusi_numero);
    }

    //tehdaan uusi alkio, etsitaan sen paikka ja lisataan se sinne
    public void lisaa_alkio(String nimi, String numero, String vanha_nimi){
        TextView lisattava = tee_tieto_alkio(nimi, numero);
        int lisays_indeksi = loyda_lisattava_indeksi(nimi.toUpperCase(), vanha_nimi);
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
    }

    public void muokkaus_pois() {
        muokkaus = false;
        muokkaa_nappi.setText(R.string.muokkaus_paalle);
        lisaa_nappi.setVisibility(View.VISIBLE);
        toteuta_muokkaus_nappi.setVisibility(View.GONE);
        alusta_tekstit();
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
        Cursor data = tietokanta.getAllData();
        int indeksi =0;
        boolean loytyi = false;

        //kaydaan data lapi ja pysahdytaan kun jonkun nimen alkuosa vastaa hakuavainta
        while (data.moveToNext()) {
            String nimi_tieto = data.getString(1);
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
        Cursor data = tietokanta.getAllData();
        int indeksi = 0 ;

        //kaydaan dataa lapi kunnes loytyy aakkosittain myohempana oleva nimi
        while (data.moveToNext()) {
            String nimi_tieto = data.getString(1);

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
        Cursor data = tietokanta.getAllData();
        int indeksi = 0;
        while (data.moveToNext()) {
            String nimi_tieto = data.getString(1);
            if (nimi_tieto.equals(nimi.toUpperCase())){
                break;
            }
            indeksi++;
        }
        return indeksi;
    }

    //apufunktioita selkeyttamaan koodia
    public boolean onko_tyhja(){
        Cursor data = tietokanta.getAllData();
        return data.getCount() == 0;
    }

    public void alusta_tekstit(){
        nimi_teksti.setText("");
        numero_teksti.setText("");
        //TODO TÄHÄN MUUTKIN
    }

    //tama funktion luo validia dataa testauksen helpottamista varten
    public void lisaa_tarkistus_dataa(){
        String[] nimet = {"aino","matti","jukka","kari","ville","pekka","riku","joona","suvi","jarno","kalle",
        "akseli","teemu","jari","olli","raimo","anna","aatu","laura","ritva","tuija","kaarlo","johanna","mari"};
        //TODO TÄHÄN VIELÄ NIMPPARIT JA SYNTTARIT
        int j = 104;
        for (String nimi: nimet){
            String numero = "0456747" + j;
            if (tarkista_syotteet(nimi.toUpperCase(), numero, "", "")) {
                lisaa_alkio(nimi.toUpperCase(),  numero,"");
                //tietokanta.insertData(nimi.toUpperCase(), numero);
            }
            j++;
        }
    }
}
