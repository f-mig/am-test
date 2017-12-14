package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementación para registrar una llamada.<br/>
 * Las llamadas se registran en un <code>Map[callId, Employee]</code> el cual asocia la llamada con el empleado<br/>
 * que la atendió.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallRegistrationMap implements CallRegistrationAware {

    private static final Logger log = LogManager.getLogger(CallRegistrationMap.class);
    private static CallRegistrationAware instance;
    private final BlockingQueue<Employee> employeesInCallProcOrder;

    /**
     * Esta clase se instancia como Singleton sólo desde el <code>main thread</code>. La referencia del Singleton se<br/>
     * pasa a los threads que accedan a él, por lo que no hace falta implementar un mecanismo thread-safe para<br/>
     * realizar la instanciación, como por ejemplo double-checked locking.<br/>
     *
     * @return Una instancia (siempre la misma) de <code>CallRegistrationMap</code>.
     * @param callRegSize
     */
    public static CallRegistrationAware getInstance(int callRegSize) {
        if (instance == null) {
            instance = new CallRegistrationMap(callRegSize);
        }
        return instance;
    }

    private CallRegistrationMap(int callRegSize) {
        this.employeesInCallProcOrder = new ArrayBlockingQueue<Employee>(callRegSize);
    }

    @Override
    public void addEmployeeInCallProcessingOrder(Employee employee) {

        if (employee != null) {
            try {
                employeesInCallProcOrder.put(employee);
            } catch (InterruptedException e) {
                log.error("Exception mientras se registraba el orden de procesamiento del empleado {}", employee);
            }
        }
    }

    public String getEmployeeTypeFromQueue() {
        return employeesInCallProcOrder.poll().getClass().getSimpleName();
    }

    public void printEmployeeCallProcessingOrder() {
        employeesInCallProcOrder.forEach(log::info);
    }

    public int getRegSizeRemainingCapacity() {
        return employeesInCallProcOrder.remainingCapacity();
    }
}
