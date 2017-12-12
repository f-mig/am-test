package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementación para registrar una llamada.<br/>
 * Las llamadas se registran en un <code>Map[callId, Employee]</code> el cual asocia la llamada con el empleado<br/>
 * que la atendió.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public class CallRegistrationMap implements CallRegistrationAware {

    private static CallRegistrationAware instance;
    private ConcurrentMap<Integer, Employee> callToEmployeeMap = new ConcurrentHashMap<>();

    /**
     * Esta clase se instancia como Singleton sólo desde el <code>main thread</code>. La referencia del Singleton se<br/>
     * pasa a los threads que accedan a él, por lo que no hace falta implementar un mecanismo thread-safe para<br/>
     * realizar la instanciación, como por ejemplo double-checked locking.<br/>
     *
     * @return Una instancia (siempre la misma) de <code>CallRegistrationMap</code>.
     */
    public static CallRegistrationAware getInstance() {
        if (instance == null) {
            instance = new CallRegistrationMap();
        }
        return instance;
    }

    @Override
    public void registerCall(Employee employee, EmployeeHandler employeeHandler, Call call) {

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
