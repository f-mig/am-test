package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallTraceCallList implements CallTraceAware {

    private static CallTraceAware instance;
    private ConcurrentMap<Integer, Employee> callToEmployeeMap = new ConcurrentHashMap<>();

    public static CallTraceAware getInstance() {
        if (instance == null) {
            instance = new CallTraceCallList();
        }
        return instance;
    }

    @Override
    public void traceCall(Employee employee, EmployeeHandler employeeHandler, Call call) {

        if (employee != null) {
            callToEmployeeMap.put(call.getId(), employee);
        }
    }

    public String getEmployeeNameByCallId(int callId) {
        return callToEmployeeMap.get(callId).getName();
    }

    public void printCallToEmployeeMap() {
        callToEmployeeMap.forEach( (k, v) -> System.out.println("[" + k + "," + v + "]") );
    }
}
