package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.model.Supervisor;

import java.util.concurrent.BlockingQueue;

/**
 * Handler encargado de manejar a los empleados Supervisores.<p/>
 *
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class SupervisorHandler extends EmployeeHandler<Supervisor> {

    /**
     * Constructor del Handler.
     *
     * @param employees Una blocking queue de Supervisores.
     * @param successorHandler El handler que será invocado en caso de que ningún Supervisor esté disponible para<br/>
     *                         atender la llamada.
     */
    public SupervisorHandler(BlockingQueue<Supervisor> employees, EmployeeHandler<? extends Employee> successorHandler) {
        this.employees = employees;
        this.successorHandler = successorHandler;
    }
}
