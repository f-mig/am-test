package com.fmigliaro.almundo.task;

import com.fmigliaro.almundo.model.*;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class CallProcessingTask implements Runnable {

    private static final int RETRY_TIMEOUT_SEC = 2;
    private Call call;
    private int callDurationLowerBoundSec;
    private int callDurationUpperBoundSec;

    private BlockingQueue<Operator> operators;
    private BlockingQueue<Supervisor> supervisors;
    private BlockingQueue<Director> directors;

    public CallProcessingTask(Call call, int callDurationLowerBoundSec, int callDurationUpperBoundSec, BlockingQueue<Operator> operators,
                              BlockingQueue<Supervisor> supervisors, BlockingQueue<Director> directors) {
        this.call = call;
        this.callDurationLowerBoundSec = callDurationLowerBoundSec;
        this.callDurationUpperBoundSec = callDurationUpperBoundSec;
        this.operators = operators;
        this.supervisors = supervisors;
        this.directors = directors;
    }

    /**
     * Utilizado en los unit tests.
     *
     */
    public CallProcessingTask(Call call, long callStartTimeMs, long callEndTimeMs, BlockingQueue<Operator> operators,
                              BlockingQueue<Supervisor> supervisors, BlockingQueue<Director> directors) {
        this.call = call;
        this.call.setStartTimeMs(callStartTimeMs);
        this.call.setEndTimeMs(callEndTimeMs);
        this.operators = operators;
        this.supervisors = supervisors;
        this.directors = directors;
    }

    @Override
    public void run() {

        boolean employeeAvailable = false;

        while (!employeeAvailable) {

            final Operator operator = operators.poll();
            if (operator != null) {
                processCall(operator, operators);
                employeeAvailable = true;
            }
            final Supervisor supervisor = supervisors.poll();
            if (supervisor != null) {
                processCall(supervisor, supervisors);
                employeeAvailable = true;
            }
            final Director director = directors.poll();
            if (director != null) {
                processCall(director, directors);
                employeeAvailable = true;
            }
            //Wait 2 seconds before retrying
            try {
                TimeUnit.SECONDS.sleep(RETRY_TIMEOUT_SEC);
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted while waiting for an available operator before retrying");
            }
        }
    }

    private <T extends Employee> void processCall(T employee, BlockingQueue<T> employees) {

        final Random rndDurationMs = new Random();
        final int taskDurationSec = rndDurationMs.nextInt(callDurationUpperBoundSec + 1) + callDurationLowerBoundSec;

        try {
            TimeUnit.SECONDS.sleep(taskDurationSec);

        } catch (InterruptedException e) {
            System.out.println("Exception thrown while processing Call: " + e.getMessage());

        } finally {
            //Return the employee back to their queue
            try {
                employees.put(employee);

            } catch (InterruptedException e) {
                System.out.println("Exception thrown while trying to put back the employee to its corresponding queue: "
                        + e.getMessage());
            }
        }
    }

}
