package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.model.Supervisor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class SupervisorHandler extends EmployeeHandler<Supervisor> {

    private static SupervisorHandler instance;

    public static SupervisorHandler getInstance(BlockingQueue<Supervisor> employees, EmployeeHandler<? extends Employee> successorHandler) {

        if (instance == null) {
            instance = new SupervisorHandler(employees, successorHandler);
        }
        return instance;
    }

    private SupervisorHandler(BlockingQueue<Supervisor> employees, EmployeeHandler<? extends Employee> successorHandler) {
        this.employees = employees;
        this.successorHandler = successorHandler;
    }
}
