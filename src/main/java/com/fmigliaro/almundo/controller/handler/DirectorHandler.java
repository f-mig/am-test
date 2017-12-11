package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class DirectorHandler extends EmployeeHandler<Director> {

    private static final Logger log = LogManager.getLogger(DirectorHandler.class);
    private static final int TIME_BETWEEN_RETRIES_SEC = 3;
    private static DirectorHandler instance;

    public static DirectorHandler getInstance(BlockingQueue<Director> employees) {

        if (instance == null) {
            instance = new DirectorHandler(employees);
        }
        return instance;
    }

    private DirectorHandler(BlockingQueue<Director> employees) {
        this.employees = employees;
    }

    @Override
    void postProcess() {
        try {
            log.info("No employees available to process call, waiting 3 seconds before retrying...");
            TimeUnit.SECONDS.sleep(TIME_BETWEEN_RETRIES_SEC);

        } catch (InterruptedException ie) {
            log.error("Interrupted while waiting before retrying employee availability", ie);
        }
    }

    public void setSuccessorHandler(EmployeeHandler<? extends Employee> successorHandler) {
        this.successorHandler = successorHandler;
    }
}
