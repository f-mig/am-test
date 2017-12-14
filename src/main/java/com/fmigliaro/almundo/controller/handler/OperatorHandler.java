package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.model.Operator;

import java.util.concurrent.BlockingQueue;

/**
 * Handler encargado de manejar a los empleados Operadores.<p/>
 *
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class OperatorHandler extends EmployeeHandler<Operator> {

    /**
     * Constructor del Handler.
     *
     * @param employees Una blocking queue de Operadores.
     * @param successorHandler El handler que será invocado en caso de que ningún Operador esté disponible para<br/>
     *                         atender la llamada.
     */
    public OperatorHandler(BlockingQueue<Operator> employees, EmployeeHandler<? extends Employee> successorHandler) {
        this.employees = employees;
        this.successorHandler = successorHandler;
    }
}
