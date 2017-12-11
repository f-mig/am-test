package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.DirectorHandler;
import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.controller.handler.OperatorHandler;
import com.fmigliaro.almundo.controller.handler.SupervisorHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Operator;
import com.fmigliaro.almundo.model.Supervisor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class DispatcherTest {

    private static final Logger log = LogManager.getLogger(DispatcherTest.class);

    private static final int THREAD_POOL_SIZE = 4;
    private static final int WORK_QUEUE_SIZE = 2;

    private static ThreadPoolExecutor threadPoolExecutor;
    private BlockingQueue<Runnable> workQueue;

    @Before
    public void setUp() throws Exception {

        workQueue = new ArrayBlockingQueue<>(WORK_QUEUE_SIZE);

        threadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, Long.MAX_VALUE,
                TimeUnit.NANOSECONDS, workQueue, new ThreadPoolExecutor.AbortPolicy());
    }

    @Test
    public void dispatchCalls() throws InterruptedException {

        final BlockingQueue<Operator> operators = new ArrayBlockingQueue<>(2);
        final BlockingQueue<Supervisor> supervisors = new ArrayBlockingQueue<>(1);
        final BlockingQueue<Director> directors = new ArrayBlockingQueue<>(1);

        operators.add(new Operator("Op1"));
        operators.add(new Operator("Op2"));
        supervisors.add(new Supervisor("Sup1"));
        directors.add(new Director("Dir1"));

        final List<Call> calls = createCalls(Arrays.asList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));

        final EmployeeHandler<Director> dirHandler = DirectorHandler.getInstance(directors);
        final EmployeeHandler<Supervisor> supHandler = SupervisorHandler.getInstance(supervisors, dirHandler);
        final EmployeeHandler<Operator> opHandler = OperatorHandler.getInstance(operators, supHandler);
        ((DirectorHandler) dirHandler).setSuccessorHandler(opHandler);

        final Dispatcher dispatcher = Dispatcher.getInstance(threadPoolExecutor, opHandler, workQueue, calls);

        dispatcher.dispatchCall();

        TimeUnit.SECONDS.sleep(30);
        threadPoolExecutor.shutdown();
    }

    private List<Call> createCalls(List<Integer> callDurations) {

        final List<Call> calls = new ArrayList<>(callDurations.size());

        for (Integer duration : callDurations) {
            calls.add(new Call(duration));
        }
        return calls;
    }

}