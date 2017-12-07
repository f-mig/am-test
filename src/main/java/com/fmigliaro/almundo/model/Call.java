package com.fmigliaro.almundo.model;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Call {

    private int id;
    private long startTimeMs;
    private long endTimeMs;

    public Call(int id, long startTimeMs, long endTimeMs) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }
}
