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

    private static SupervisorHandler instance;

    /**
     * Esta clase se instancia como Singleton sólo desde el <code>main thread</code>. La referencia del Singleton se<br/>
     * pasa a los threads que accedan a él, por lo que no hace falta implementar un mecanismo thread-safe para<br/>
     * realizar la instanciación, como por ejemplo double-checked locking.<br/>
     *
     * @param employees Una blocking queue de Supervisores.
     * @param successorHandler El handler que será invocado en caso de que ningún Supervisor esté disponible para<br/>
     *                         atender la llamada.
     * @return Una instancia (siempre la misma) de <code>{@link SupervisorHandler}</code>.
     */
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
