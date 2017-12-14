package com.fmigliaro.almundo.model;

import java.util.Random;

/**
 * POJO que representa a una llamada con su id y duración.<br/>
 * De acuerdo a la forma de instanciar a esta clase, la duración puede ser inyectada o calculada aleatoriamente.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Call {

    private static final Random rndDurationMs = new Random();
    private static int idSeq = 1;

    private int id;
    private int durationMs;

    /**
     * Este constructor, dado una duración mínima y duración máxima, determina la duración de la llamada generando<br/>
     * un valor entero aleatorio que se encuentre dentro del rango [duración min, duración max].
     *
     * @param durationStartOffsetMs Límite inferior del rango dentro del cual se genera la duración aleatoria.
     * @param durationRangeSizeMs Límite superior del rango dentro del cual se genera la duración aleatoria.
     */
    public Call(int durationStartOffsetMs, int durationRangeSizeMs) {
        this.id = idSeq++;
        this.durationMs =  durationStartOffsetMs + rndDurationMs.nextInt(durationRangeSizeMs + 1);
    }

    /**
     * Este constructor se utiliza para testing unitario ya que permite inyectar la duración de cada llamada en lugar<br/>
     * de que cada llamada determine su propia duración de manera aleatoria. Esto permite realizar tests determinísticos.<br/>
     *
     * @param durationMs La duración de llamada en milisegundos.
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
        return "Llamada[id=" + id + "][duracion="+ durationMs + "ms]";
    }
}
