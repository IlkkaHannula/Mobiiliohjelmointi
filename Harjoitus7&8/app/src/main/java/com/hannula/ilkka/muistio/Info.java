package com.hannula.ilkka.muistio;

/**
 * Created by Ile on 23.2.2018.
 */

public class Info {
    private String nimi;
    private String numero;
    private String synttari;
    private String nimppari;

    public Info() {}

    public Info(String nimi, String numero, String synttari, String nimppari) {
        this.nimi = nimi;
        this.numero = numero;
        this.synttari = synttari;
        this.nimppari = nimppari;
    }

    public void Modify(String nimi, String numero, String synttari, String nimppari) {
        this.nimi = nimi;
        this.numero = numero;
        this.synttari = synttari;
        this.nimppari = nimppari;

    }

    public String getNimi() {
        return nimi;
    }
    public String getNumero() {
        return numero;
    }
    public String getSynttari() {
        return synttari;
    }
    public String getNimppari() {
        return nimppari;
    }
}


