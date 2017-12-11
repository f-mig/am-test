package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;

/**
 * Created by Francisco Migliaro on 11/12/2017.
 */
public interface CallTraceAware {

    void traceCall(Employee employee, EmployeeHandler employeeHandler, Call call);
}
