package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.utility.CallRegistrationAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase abstracta que contiene la lógica en común de los handlers de empleados: {@link OperatorHandler},<br/>
 * {@link SupervisorHandler} y {@link DirectorHandler}.<br/>
 * Se encarga del manejo y procesamiento de las llamadas, siendo las clases hija las responsables<br/>
 * de definir cualquier comportamiento específico sobre dicho procesamiento.
 * <p/>
 * Created by Francisco Migliaro on 10/12/2017.
 *
 */
public abstract class EmployeeHandler<T extends Employee> {

    private static final Logger log = LogManager.getLogger(EmployeeHandler.class);
    BlockingQueue<T> employees;
    EmployeeHandler<? extends Employee> successorHandler;

    public abstract String getNoEmployeesAvailableMsg();

    /**
     * Se encarga del manejo de la llamada pasada por parámetro. Para ello, intenta obtener algún empleado disponible<br/>
     * para procesar dicha llamada (cada clase hija se encargará de buscar los empleados que le correspondan). En caso<br/>
     * de no encontrar empleados disponibles, delega la búsqueda al handler que tiene definido como sucesor, el cual<br/>
     * repetirá el proceso con sus propios empleados. Este proceso se repite <b>de manera indefinida</b> hasta que se<br/>
     * libere algún empleado y pueda procesarse la llamada.
     *
     * @param call La llamada que se pretende procesar.
     * @param callReg Objeto que permite registrar cada llamada que fue procesada y asociarla con el empleado que la
     *                procesó. <br/>Este registro se utiliza en los tests unitarios o para loggear para debug.
     */
    public void handleCall(Call call, CallRegistrationAware callReg) {

        final T employee = employees.poll();

        if (employee != null) {
            processCall(call, employee, callReg);
            return;
        }
        postProcess(call, callReg);
    }

    /**
     * Método auxiliar que simula procesamiento de una llamada pasada por parámetro.<br/>
     * Cada llamada determina cuál será su duración de manera aleatoria y al momento de ser instanciada, y<br/>
     * dicha duración <code>t</code> representa el tiempo de procesamiento simulado. Para lograr dicho procesamiento,<br/>
     * el método pone en <b>sleep</b> una cantidad de tiempo <code>t</code> al thread que está procesando la llamada.
     *
     * @param call La llamada que se desea simular su procesamiento.
     * @param employee El empleado que procesa la llamada; sólo para fines de logging.
     */
    private void processCall(Call call, T employee, CallRegistrationAware callReg) {

        try {
            TimeUnit.MILLISECONDS.sleep(call.getDurationMs());
            callReg.registerCall(employee, this, call);

        } catch (InterruptedException ie) {
            log.error("Exception mientras se procesaba la llamada: ", ie);

        } finally {
            //Return the employee back to their queue
            try {
                //log.info("Colocando al {} de nuevo en su cola...", employee);
                employees.put(employee);

            } catch (InterruptedException ie) {
                log.error("Exception mientras se intentaba colocar al empleado de nuevo en su cola: ", ie);
            }
        }
    }

    synchronized void postProcess(Call call, CallRegistrationAware callReg) {
        successorHandler.handleCall(call, callReg);
    }
}
