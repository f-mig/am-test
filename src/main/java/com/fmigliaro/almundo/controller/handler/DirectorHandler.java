package com.fmigliaro.almundo.controller.handler;

import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
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
    private static DirectorHandler instance;
    private static int timeBeforeRetryMs;

    /**
     * Esta clase se instancia como Singleton sólo desde el <code>main thread</code>. La referencia del Singleton se<br/>
     * pasa a los threads que accedan a él, por lo que no hace falta implementar un mecanismo thread-safe para<br/>
     * realizar la instanciación, como por ejemplo double-checked locking.<br/><br/>
     * Este handler no recibe por parámetro al <b>handler sucesor</b> en su método getInstance(), como lo hacen el<br/>
     * resto de los handlers. Esto se debe a que el handler sucesor es {@link OperatorHandler}, el cual no puede<br/>
     * instanciarse antes que lo haga {@link DirectorHandler}.<br/>
     * La forma de resolverlo es inyectar la dependencia mediante un setter luego de la creación del <code>OperatorHandler</code>.
     *
     * @param employees Una blocking queue de Directores.
     * @return Una instancia (siempre la misma) de <code>{@link DirectorHandler}</code>.
     */
    public static DirectorHandler getInstance(BlockingQueue<Director> employees, int timeBeforeRetryMs) {

        if (instance == null) {
            instance = new DirectorHandler(employees);
            DirectorHandler.timeBeforeRetryMs = timeBeforeRetryMs;
        }
        return instance;
    }

    private DirectorHandler(BlockingQueue<Director> employees) {
        this.employees = employees;
    }

    /**
     * Este handler tiene un compartamiento ligeramente diferente al de los dos handlers restantes, en lo referido<br/>
     * a qué hacer luego de que ningún empleado esté disponible. El resto de los handlers, simplemente delega la tarea<br/>
     * al handler sucesor. En cambio, {@link DirectorHandler} debe esperar unos segundos, antes de delegar al handler<br/>
     * sucesor ({@link OperatorHandler}), y recomenzar el proceso de intento nuevamente.
     *
     * @param call La llamada que este handler debe procesar. Se utiliza para propósito de logging.
     */
    @Override
    void postProcess(Call call, CallRegistrationAware callReg) {

        try {
            log.info("No hay empleados disponibles para procesar la {}. Esperando {} ms para reintentar...",
                    call, timeBeforeRetryMs);

            TimeUnit.MILLISECONDS.sleep(timeBeforeRetryMs);
            handleCall(call, callReg);

        } catch (InterruptedException ie) {
            log.error("Exception mientras se esperaba antes de reintentar encontrar a un empleado disponible: ", ie);
        }
    }

    @Override
    public String getNoEmployeesAvailableMsg() {
        return "no hay Directores disponibles.";
    }
}
