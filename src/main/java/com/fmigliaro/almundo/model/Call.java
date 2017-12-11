package com.fmigliaro.almundo.model;

import java.util.Random;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Call {

    private static final Random rndDurationMs = new Random();
    private static int idSeq = 1;

    private int id;
    private int durationMs;

    public Call(int durationMinMs, int durationMaxMs) {
        this.id = idSeq++;
        this.durationMs =  durationMinMs + rndDurationMs.nextInt(durationMaxMs + 1);
    }

    /**
     * Used only for unit testing.
     * @param durationMs The call duration in milliseconds.
     */
    public Call(int durationMs) {
        this.id = idSeq++;
        this.durationMs = durationMs;
    }

    public int getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "Call[id=" + id + "][duration="+ durationMs + "ms]";
    }
}
