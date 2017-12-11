package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallTraceLogger implements CallTraceAware {

    private static final Logger log = LogManager.getLogger(CallTraceLogger.class);
    private static CallTraceAware instance;

    public static CallTraceAware getInstance() {
        if (instance == null) {
            instance = new CallTraceLogger();
        }
        return instance;
    }

    @Override
    public void traceCall(Employee employee, EmployeeHandler employeeHandler, Call call) {

        if (employee != null){
            log.info("Got {} to process {}", employee, call);
        } else {
            log.info("Polling to process {} but {}", call, employeeHandler.getNoEmployeesAvailableMsg());
        }
    }
}
