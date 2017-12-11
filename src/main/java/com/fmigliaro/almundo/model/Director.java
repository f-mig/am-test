package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Director extends Employee {

    public Director(String name) {
        setName(name);
    }

    @Override
    public String toString() {
        return "Director[name=" + getName() + "]";
    }
}
