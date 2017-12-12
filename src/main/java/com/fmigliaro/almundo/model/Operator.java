package com.fmigliaro.almundo.model;

/**
 * POJO que Representa a un empleado Operador.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Operator extends Employee {

    public Operator(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Operador[nombre=" + getName() + "]";
    }
}
