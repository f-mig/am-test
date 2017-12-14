package com.fmigliaro.almundo.utility;

import com.fmigliaro.almundo.model.Employee;

/**
 * Interfaz que define un método para registrar el orden de procesamiento de las llamadas por los empleados.<br/>
 * En particular, lo que se desea registrar son los empleados en el <b>orden que atendieron las llamadas</b>.<br/>
 * Esto permite facilitar el testing unitario o simplemente loggear la información que luego puede ser utilizada para<br/>
 * debugging.<br/><br/>
 * Cómo y dónde se registra la información, depende de cada implementación.<p/>
 *
 * Created by Francisco Migliaro on 11/12/2017.
 */
public interface CallRegistrationAware {

    void addEmployeeInCallProcessingOrder(Employee employee);

}
