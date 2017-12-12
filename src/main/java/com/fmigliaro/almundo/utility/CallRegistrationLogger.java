package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementación para registrar una llamada.<br/>
 * Las llamadas se registran en un log, el cual asocia la llamada con el empleado que la atendió.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallRegistrationLogger implements CallRegistrationAware {

    private static final Logger log = LogManager.getLogger(CallRegistrationLogger.class);
    private static CallRegistrationAware instance;

    public static CallRegistrationAware getInstance() {
        if (instance == null) {
            instance = new CallRegistrationLogger();
        }
        return instance;
    }

    @Override
    public void registerCall(Employee employee, EmployeeHandler employeeHandler, Call call) {

        if (employee != null){
            log.info("El empleado {} procesará la {}", employee, call);
        } else {
            log.info("Intentando procesar la {} pero {}", call, employeeHandler.getNoEmployeesAvailableMsg());
        }
    }
}
