package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Operator extends Employee {

    public Operator(String name) {
        setName(name);
    }

    @Override
    public String toString() {
        return "Operator[name=" + getName() + "]";
    }
}
