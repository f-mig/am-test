package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Supervisor extends Employee {

    public Supervisor(String name) {
        setName(name);
    }

    @Override
    public String toString() {
        return "Supervisor[name=" + getName() + "]";
    }
}
