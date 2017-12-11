package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.model.Operator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class OperatorHandler extends EmployeeHandler<Operator> {

    private static OperatorHandler instance;

    public static OperatorHandler getInstance(BlockingQueue<Operator> employees, EmployeeHandler<? extends Employee> successorHandler) {

        if (instance == null) {
            instance = new OperatorHandler(employees, successorHandler);
        }
        return instance;
    }

    private OperatorHandler(BlockingQueue<Operator> employees, EmployeeHandler<? extends Employee> successorHandler) {
        this.employees = employees;
        this.successorHandler = successorHandler;
    }

    @Override
    public String getNoEmployeesAvailableMsg() {
        return "no Operators available.";
    }
}
