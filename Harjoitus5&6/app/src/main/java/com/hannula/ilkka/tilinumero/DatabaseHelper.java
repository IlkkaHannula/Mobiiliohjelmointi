package com.hannula.ilkka.tilinumero;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;


//maaritellaan tassa tietokannan tiedot
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TIETOKANNAN_NIMI = "Tilinumerot.db";
    public static final String TAULUKON_NIMI = "Tilinumeroita";
    public static final String SARAKE_1 = "ID";
    public static final String SARAKE_2 = "NIMI";
    public static final String SARAKE_3 = "NUMERO";
    public static final String SARAKE_4 = "SYNTYMAPAIVA";
    public static final String SARAKE_5 = "NIMIPAIVA";


    public DatabaseHelper(Context context) {
        super(context, TIETOKANNAN_NIMI, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TAULUKON_NIMI + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, NIMI TEXT, NUMERO TEXT, SYNTYMAPAIVA DATE, NIMIPAIVA TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TAULUKON_NIMI);
        onCreate(db);

    }

    //kerataan ensin tiedot ja sitten lisataan ne tietokantaan
    public boolean insertData(String nimi, String numero, Date syntymapaiva, String nimipaiva){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SARAKE_2, nimi);
        contentValues.put(SARAKE_3, numero);
        contentValues.put(SARAKE_4, syntymapaiva);
        contentValues.put(SARAKE_5, nimipaiva);
        long result = db.insert(TAULUKON_NIMI,null,contentValues);
        return (result != -1);
    }

    //jos jotain dataa halutaan muutta tehdaan ensin uudet tiedot ja paivitetaan
    public boolean updateData(String id, String uusi_nimi, String uusi_numero){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SARAKE_1, id);
        contentValues.put(SARAKE_2, uusi_nimi);
        contentValues.put(SARAKE_3, uusi_numero);
        db.update(TAULUKON_NIMI, contentValues, "ID = ?", new String[] {id});
        return true;
    }

    public boolean deleteData(String id){
        //poistetaan data halutulta idlta
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TAULUKON_NIMI, SARAKE_1 + "=" + id, null) > 0;    }

    public Cursor getAllData(){
        //haetaan datat ja palautetaan ne aakkosjarjestyksessa
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(TAULUKON_NIMI, null, null, null, null, null, SARAKE_2+" ASC");
    }
}