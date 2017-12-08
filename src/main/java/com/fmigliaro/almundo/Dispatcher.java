package com.fmigliaro.almundo;

import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Operator;
import com.fmigliaro.almundo.model.Supervisor;
import com.fmigliaro.almundo.task.CallProcessingTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class Dispatcher {

    private final ExecutorService executorService;
    private final int callDurationLower;
    private final int callDurationUpper;
    private final BlockingQueue<Operator> operators;
    private final BlockingQueue<Supervisor> supervisors;
    private final BlockingQueue<Director> directors;

    public Dispatcher(ExecutorService executorService, int callDurationLower, int callDurationUpper,
                      BlockingQueue<Operator> operators, BlockingQueue<Supervisor> supervisors,
                      BlockingQueue<Director> directors) {

        this.executorService = executorService;
        this.callDurationLower = callDurationLower;
        this.callDurationUpper = callDurationUpper;
        this.operators = operators;
        this.supervisors = supervisors;
        this.directors = directors;
    }

    public void dispatchCall(Call call) {

        executorService.submit(() -> new CallProcessingTask(
                call,
                callDurationLower,
                callDurationUpper,
                operators,
                supervisors,
                directors)
        );
    }
}

