package com.fmigliaro.almundo.model;

/**
 * POJO que Representa a un empleado Director.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Director extends Employee {

    public Director(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Director[nombre=" + getName() + "]";
    }
}
