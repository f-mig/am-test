package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Employee;

/**
 * Interfaz que define un método para registrar el procesamiento de las llamadas. En particular, lo que se desea<br/>
 * registrar es qué empleado atendió cada llamada realizada. Esto permite facilitar el testing unitario o simplemente<br/>
 * loggear la información para luego ser utilizada en debugging.<br/><br/>
 * Cómo y dónde se registra la información, depende de cada implementación.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public interface CallRegistrationAware {

    void registerCall(Employee employee, EmployeeHandler employeeHandler, Call call);

}
