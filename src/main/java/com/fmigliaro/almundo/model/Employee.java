package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public abstract class Employee {

    private String name;
    private Call call;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    Call getCall() {
        return call;
    }

    void setCall(Call call) {
        this.call = call;
    }

}
