package com.fmigliaro.almundo.model;

/**
 * Clase abstracta que representa a un empleado.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public abstract class Employee {

    private String name;

    public Employee(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
