package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Francisco Migliaro on 10/12/2017.
 */
public abstract class EmployeeHandler<T extends Employee> {

    private static final Logger log = LogManager.getLogger(EmployeeHandler.class);
    BlockingQueue<T> employees;
    EmployeeHandler<? extends Employee> successorHandler;

    public boolean handleCall(Call call) {

        final T employee = employees.poll();

        log.info(employee != null ? "is polling " + employee : " No employees available");

        if (employee != null) {
            processCall(call, employee);
            return true;
        }
        postProcess();
        return successorHandler.handleCall(call);
    }

    private void processCall(Call call, T employee) {

        try {
            log.info("{} is processing {}...", employee, call);
            TimeUnit.MILLISECONDS.sleep(call.getDurationMs());

        } catch (InterruptedException ie) {
            log.error("Exception thrown while processing call: ", ie);

        } finally {
            //Return the employee back to their queue
            try {
                log.info("Putting back {} into their queue...", employee);
                employees.put(employee);

            } catch (InterruptedException ie) {
                log.error("Exception thrown while trying to put back employee into their queue: ", ie);
            }
        }
    }

    void postProcess() {
        //Empty default implementation
    }
}
