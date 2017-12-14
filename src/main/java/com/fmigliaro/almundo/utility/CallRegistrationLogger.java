package com.fmigliaro.almundo.utility;

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

    public CallRegistrationLogger() {

    }

    @Override
    public void addEmployeeInCallProcessingOrder(Employee employee) {

        if (employee != null){
            log.info("El empleado {} procesó la llamada", employee);
        }
    }
}
