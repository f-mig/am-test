package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public abstract class Employee {

    private String name;
    private Call call;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }
}
