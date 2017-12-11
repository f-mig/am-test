package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.utility.CallTraceAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
class Dispatcher {

    private static final Logger log = LogManager.getLogger(Dispatcher.class);
    private static final int REJECTED_TASK_WAIT_SEC = 3;

    private static Dispatcher instance = null;

    private final List<Call> calls;
    private final ExecutorService executorService;
    private final EmployeeHandler employeeHandler;
    private final CallTraceAware callTracer;

    static Dispatcher getInstance(ExecutorService executorService, EmployeeHandler employeeHandler, List<Call> calls, CallTraceAware callTracer) {
        if (instance == null) {
            instance = new Dispatcher(executorService, employeeHandler, calls, callTracer);
        }
        return instance;
    }

    private Dispatcher(ExecutorService executorService, EmployeeHandler employeeHandler, List<Call> calls, CallTraceAware callTracer) {
        this.executorService = executorService;
        this.employeeHandler = employeeHandler;
        this.calls = calls;
        this.callTracer = callTracer;
    }

    void dispatchCalls() throws InterruptedException {

        if (calls == null || calls.isEmpty()) {
            log.error("Cannot process calls if call list is null or empty. Exiting application...");
            return;
        }
        for (Call call : calls) {
            try {
                log.info("Submitting new task to handle {}", call);
                executorService.submit(() -> employeeHandler.handleCall(call, callTracer));

            } catch (RejectedExecutionException ree) {
                log.warn("{} couldn't be processed: all threads are busy and work queue is full. Discarding " +
                        "call and waiting 3 seconds before processing next one...", call);
                TimeUnit.SECONDS.sleep(REJECTED_TASK_WAIT_SEC);
            }
        }
    }
}
