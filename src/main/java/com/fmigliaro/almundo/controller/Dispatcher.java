package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.utility.CallRegistrationAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Esta clase se encarga de despachar las llamadas. Las mismas son inyectadas en el Dispatcher al momento de su<br/>
 * creación. Esto es a efectos ilustrativos, dado que en un caso real, el Dispatcher estaría quizás escuchando en una<br/>
 * cola de mensajes en donde cada mensaje representa una llamada.<br/><br/>
 * El Dispatcher procesa las llamadas de manera concurrente: cada llamada que recibe, intenta despacharla enviándola a<br/>
 * uno de los threads disponibles en un pool de threads. El pool de threads se configuró con un tamaño fijo, por lo que<br/>
 * en caso de que las llamadas recibidas no puedan ser abastecidas por estar todos los threads del pool ocupados, el<br/>
 * {@link java.util.concurrent.ThreadPoolExecutor} que maneja el pool, guardará en una queue interna los Runnables que<br/>
 * no pudieron ser inmediatamente atendidos. Cuando exista algún thread disponible, consumirá de la cola interna los<br/>
 * Runnables y estos serán procesado por el thread.<br/><br/>
 * En el caso de que llegue una llamada y no haya lugar ni el pool ni en la cola interna, se procederá a descartar dicha<br/>
 * llamada.<p/>
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
class Dispatcher {

    private static final Logger log = LogManager.getLogger(Dispatcher.class);
    private static final int REJECTED_TASK_WAIT_SEC = 3;

    private final List<Call> calls;
    private final ExecutorService executorService;
    private final EmployeeHandler employeeHandler;
    private final CallRegistrationAware callReg;

    Dispatcher(ExecutorService executorService, EmployeeHandler employeeHandler, List<Call> calls,
               CallRegistrationAware callReg) {
        this.executorService = executorService;
        this.employeeHandler = employeeHandler;
        this.calls = calls;
        this.callReg = callReg;
    }

    /**
     * Método encargado de despachar las llamadas, enviándolas de manera asincrónica para su ejecución por un pool de<br/>
     * threads. En caso de que una llamada sea rechazada por no haber threads disponibles y no haber espacio en la <br/>
     * work queue interna del thread pool, la llamada es rechazada y se esperan unos segundos antes de procesar la
     * siguiente llamada.<br/>
     *
     */
    void dispatchCalls() throws InterruptedException {

        if (calls == null || calls.isEmpty()) {
            log.error("No se pueden procesar las llamadas si la lista de llamadas es null o vacía. " +
                    "Saliendo de la aplicación...");
            return;
        }
        for (Call call : calls) {
            try {
                executorService.execute(() ->
                        employeeHandler.handleCall(call, callReg)
                );

            } catch (RejectedExecutionException ree) {
                log.warn("La {} no pudo ser procesada: todos los threads están ocupados y la cola interna está llena. " +
                        "Descartando la llamada y esperando unos segundos antes de procesar una nueva.", call);

                TimeUnit.SECONDS.sleep(REJECTED_TASK_WAIT_SEC);
            }
        }
    }
}
