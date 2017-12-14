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
 * de definir cualquier comportamiento específico sobre dicho procesamiento.<p/>
 *
 * Created by Francisco Migliaro on 10/12/2017.
 *
 */
public abstract class EmployeeHandler<T extends Employee> {

    private static final Logger log = LogManager.getLogger(EmployeeHandler.class);
    BlockingQueue<T> employees;
    EmployeeHandler<? extends Employee> successorHandler;
    private Lock pollingLock = new ReentrantLock();

    /**
     * Se encarga del manejo de la llamada pasada por parámetro. Para ello, intenta obtener algún empleado disponible<br/>
     * para procesar dicha llamada (cada clase hija se encargará de buscar los empleados que le correspondan). En caso<br/>
     * de no encontrar empleados disponibles, delega la búsqueda al handler que tiene definido como sucesor, el cual<br/>
     * repetirá el proceso con sus propios empleados. Este proceso se repite <b>de manera indefinida</b> hasta que se<br/>
     * libere algún empleado y pueda procesarse la llamada.
     *
     * @param call La llamada que se pretende procesar.
     * @param callReg Objeto que permite registrar el orden en que los empleados atendieron las llamadas.
     */
    public void handleCall(Call call, CallRegistrationAware callReg) {

        pollingLock.lock();
        final T employee;
        try {
            employee = employees.poll();
            callReg.addEmployeeInCallProcessingOrder(employee);
        } finally {
            pollingLock.unlock();
        }

        if (employee != null) {
            try {
                TimeUnit.MILLISECONDS.sleep(call.getDurationMs());

            } catch (InterruptedException ie) {
                log.error("Ocurrió una Exception mientras se procesaba la llamada: ", ie);
            } finally {
                //Dado que el empleado finalizó el procesamiento de la llamada, se lo vuelve a insertar en su
                //respectiva cola con el objeto de estar disponible para procesar una nueva llamada.
                employees.offer(employee);
            }
        } else {
            postProcess(call, callReg);
        }
    }

    void postProcess(Call call, CallRegistrationAware callReg) {
        successorHandler.handleCall(call, callReg);
    }
}
