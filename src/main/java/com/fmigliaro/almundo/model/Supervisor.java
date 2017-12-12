package com.fmigliaro.almundo.model;

/**
 * POJO que Representa a un empleado Supervisor.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Supervisor extends Employee {

    public Supervisor(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Supervisor[name=" + getName() + "]";
    }
}
