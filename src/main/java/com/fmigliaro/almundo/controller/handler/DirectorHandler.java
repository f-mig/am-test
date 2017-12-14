package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Employee;
import com.fmigliaro.almundo.utility.CallRegistrationAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handler encargado de manejar a los empleados Directores.<p/>
 *
 * Created by Francisco Migliaro on 10/12/2017.
 */
public class DirectorHandler extends EmployeeHandler<Director> {

    private static final Logger log = LogManager.getLogger(DirectorHandler.class);
    private int timeBeforeRetryMs;

    /**
     * Este handler no recibe por parámetro al <b>handler sucesor</b> en su constructor, como lo hacen el<br/>
     * resto de los handlers. Esto se debe a que el handler sucesor es {@link OperatorHandler}, el cual no puede<br/>
     * instanciarse antes que lo haga {@link DirectorHandler}.<br/>
     * La forma de resolverlo es inyectar la dependencia mediante un setter luego de la creación del <code>OperatorHandler</code>.
     *
     * @param employees Una blocking queue de Directores.
     * @param timeBeforeRetryMs Tiempo en milisegundos que se espera antes de reiniciar la búsqueda de un empleado
     *                          disponible.
     */
    public DirectorHandler(BlockingQueue<Director> employees, int timeBeforeRetryMs) {
        this.employees = employees;
        this.timeBeforeRetryMs = timeBeforeRetryMs;
    }

    /**
     * Este handler tiene un compartamiento ligeramente diferente al de los dos handlers restantes, en lo referido<br/>
     * a qué hacer luego de que ningún empleado esté disponible. El resto de los handlers, simplemente delega la tarea<br/>
     * al handler sucesor. En cambio, {@link DirectorHandler} debe esperar unos segundos, antes de delegar al handler<br/>
     * sucesor ({@link OperatorHandler}), y recomenzar el proceso de intento nuevamente.
     *
     * @param call La llamada que este handler debe procesar. Se utiliza para propósito de logging.
     * @param callReg Objeto que permite registrar el orden en que los empleados atendieron las llamadas.
     */
    @Override
    void postProcess(Call call, CallRegistrationAware callReg) {

        try {
            log.info("No hay empleados disponibles para procesar la {}. Esperando {} ms para reintentar...",
                    call, timeBeforeRetryMs);

            TimeUnit.MILLISECONDS.sleep(timeBeforeRetryMs);
            successorHandler.handleCall(call, callReg);

        } catch (InterruptedException ie) {
            log.error("Exception mientras se esperaba antes de reintentar encontrar a un empleado disponible: ", ie);
        }
    }

    public void setSuccessorHandler(EmployeeHandler<? extends Employee> successorHandler) {
        this.successorHandler = successorHandler;
    }
}
