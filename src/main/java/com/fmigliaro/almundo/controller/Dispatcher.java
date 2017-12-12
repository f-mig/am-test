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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private static Dispatcher instance = null;

    private final List<Call> calls;
    private final ExecutorService executorService;
    private final EmployeeHandler employeeHandler;
    private final CallRegistrationAware callReg;
    private final Lock lock = new ReentrantLock();

    static Dispatcher getInstance(ExecutorService executorService, EmployeeHandler employeeHandler, List<Call> calls,
                                  CallRegistrationAware callReg) {
        if (instance == null) {
            instance = new Dispatcher(executorService, employeeHandler, calls, callReg);
        }
        return instance;
    }

    private Dispatcher(ExecutorService executorService, EmployeeHandler employeeHandler, List<Call> calls,
                       CallRegistrationAware callReg) {
        this.executorService = executorService;
        this.employeeHandler = employeeHandler;
        this.calls = calls;
        this.callReg = callReg;
    }

    void dispatchCalls() throws InterruptedException {

        if (calls == null || calls.isEmpty()) {
            log.error("No se pueden procesar las llamadas si la lista de llamadas es null o vacía. " +
                    "Saliendo de la aplicación...");
            return;
        }
        for (Call call : calls) {
            try {
                log.info("Enviando a empleado {} nueva tarea para procesar {}", employeeHandler.getClass().getSimpleName(), call);
                executorService.submit(() -> employeeHandler.handleCall(call, callReg));

            } catch (RejectedExecutionException ree) {
                log.warn("La {} no pudo ser procesada: todos los threads están ocupados y la cola interna está llena. " +
                        "Descartando la llamada y esperando unos segundos antes de procesar una nueva.", call);

                TimeUnit.SECONDS.sleep(REJECTED_TASK_WAIT_SEC);
            }
        }
    }
}
