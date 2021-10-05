/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.entity;

import java.util.Date;

/**
 *
 * @author agodino
 */
public class Presenti {

    Long id;
    String nome, cognome;
    Date start, end;
    double ore, ore_riconosciute = 0;

    public Presenti(Long id, String nome, String cognome, Date start, Date end, double ore) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.start = start;
        this.end = end;
        this.ore = ore;
    }

    public Presenti(Long id, String nome, String cognome, Date start, Date end, double ore, double ore_riconosciute) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.start = start;
        this.end = end;
        this.ore = ore;
        this.ore_riconosciute = ore_riconosciute;
    }

    public Presenti() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public double getOre() {
        return ore;
    }

    public void setOre(double ore) {
        this.ore = ore;
    }

    public double getOre_riconosciute() {
        return ore_riconosciute;
    }

    public void setOre_riconosciute(double ore_riconosciute) {
        this.ore_riconosciute = ore_riconosciute;
    }

}
