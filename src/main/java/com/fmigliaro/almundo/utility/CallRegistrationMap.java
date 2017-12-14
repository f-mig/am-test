package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementación para registrar el orden en que los empleados atendieron las llamadas.<br/>
 * Los empleados se registran en una cola en el orden en que procesaron cada llamada.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallRegistrationMap implements CallRegistrationAware {

    private static final Logger log = LogManager.getLogger(CallRegistrationMap.class);
    private final BlockingQueue<Employee> employeesInCallProcOrder;

    public CallRegistrationMap(int callRegSize) {
        this.employeesInCallProcOrder = new ArrayBlockingQueue<>(callRegSize);
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
        try {
            return employeesInCallProcOrder.take().getClass().getSimpleName();
        } catch (InterruptedException e) {
            log.error(e);
            return "";
        }
    }

    public void printEmployeeCallProcessingOrder() {
        employeesInCallProcOrder.forEach(log::info);
    }

    public int getRegSizeRemainingCapacity() {
        return employeesInCallProcOrder.remainingCapacity();
    }
}
