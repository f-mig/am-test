package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.model.Call;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
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
    private final BlockingQueue<Runnable> workQueue;
    private final EmployeeHandler employeeHandler;

    static Dispatcher getInstance(ExecutorService executorService, EmployeeHandler employeeHandler,
                                  BlockingQueue<Runnable> workQueue, List<Call> calls) {
        if (instance == null) {
            instance = new Dispatcher(executorService, employeeHandler, workQueue, calls);
        }
        return instance;
    }

    private Dispatcher(ExecutorService executorService, EmployeeHandler employeeHandler, BlockingQueue<Runnable> workQueue, List<Call> calls) {
        this.executorService = executorService;
        this.employeeHandler = employeeHandler;
        this.workQueue = workQueue;
        this.calls = calls;
    }

    void dispatchCall() throws InterruptedException {

        if (calls == null || calls.isEmpty()) {
            log.error("Cannot process calls if call list is null or empty. Exiting application...");
            return;
        }
        for (Call call : calls) {

            try {
                executorService.submit(() -> employeeHandler.handleCall(call));
                log.info("Task submitted. Internal thread pool queue size: {}", workQueue.size());

            } catch (RejectedExecutionException ree) {
                log.warn("{} couldn't be processed: all threads are busy and work queue is full. Discarding " +
                        "call and waiting 3 seconds before processing next one...", call);
                TimeUnit.SECONDS.sleep(REJECTED_TASK_WAIT_SEC);
            }
        }
    }
}
