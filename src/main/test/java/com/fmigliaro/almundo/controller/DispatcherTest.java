package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.DirectorHandler;
import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.controller.handler.OperatorHandler;
import com.fmigliaro.almundo.controller.handler.SupervisorHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Operator;
import com.fmigliaro.almundo.model.Supervisor;
import com.fmigliaro.almundo.utility.CallTraceAware;
import com.fmigliaro.almundo.utility.CallTraceCallList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class DispatcherTest {

    private static final Logger log = LogManager.getLogger(DispatcherTest.class);

    private static final int THREAD_POOL_SIZE = 10;
    private static final int WORK_QUEUE_SIZE = 3;

    private static ThreadPoolExecutor threadPoolExecutor;

    @Before
    public void setUp() throws Exception {

        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(WORK_QUEUE_SIZE);

        threadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, Long.MAX_VALUE,
                TimeUnit.NANOSECONDS, workQueue, new ThreadPoolExecutor.AbortPolicy());
    }

    @Test
    public void equalNumOfCallsAndThreadsShouldProcessInCorrectOrder() throws InterruptedException {

        final BlockingQueue<Operator> operators = new ArrayBlockingQueue<>(5);
        final BlockingQueue<Supervisor> supervisors = new ArrayBlockingQueue<>(3);
        final BlockingQueue<Director> directors = new ArrayBlockingQueue<>(2);

        createEmployees(operators, supervisors, directors);

        final List<Call> calls = createCalls(Arrays.asList(2000, 2000, 2000, 2000, 2000, 1000, 1000, 1000, 500, 500));

        final int timeBeforeRetryMs = 500;
        final EmployeeHandler<Director> dirHandler = DirectorHandler.getInstance(directors, timeBeforeRetryMs);
        final EmployeeHandler<Supervisor> supHandler = SupervisorHandler.getInstance(supervisors, dirHandler);
        final EmployeeHandler<Operator> opHandler = OperatorHandler.getInstance(operators, supHandler);
        ((DirectorHandler) dirHandler).setSuccessorHandler(opHandler);

        final CallTraceAware tracer = CallTraceCallList.getInstance();
        final Dispatcher dispatcher = Dispatcher.getInstance(threadPoolExecutor, opHandler, calls, tracer);

        dispatcher.dispatchCalls();

        TimeUnit.SECONDS.sleep(7);

        final CallTraceCallList listTracer = (CallTraceCallList) tracer;
        listTracer.printCallToEmployeeMap();

        assertEquals("Op1", listTracer.getEmployeeNameByCallId(1));
        assertEquals("Op2", listTracer.getEmployeeNameByCallId(2));
        assertEquals("Op3", listTracer.getEmployeeNameByCallId(3));
        assertEquals("Op4", listTracer.getEmployeeNameByCallId(4));
        assertEquals("Op5", listTracer.getEmployeeNameByCallId(5));
        assertEquals("Sup1", listTracer.getEmployeeNameByCallId(6));
        assertEquals("Sup2", listTracer.getEmployeeNameByCallId(7));
        assertEquals("Sup3", listTracer.getEmployeeNameByCallId(8));
        assertEquals("Dir1", listTracer.getEmployeeNameByCallId(9));
        assertEquals("Dir2", listTracer.getEmployeeNameByCallId(10));

        threadPoolExecutor.shutdown();
        log.info("Thread pool was shut down");
    }

    private void createEmployees(BlockingQueue<Operator> operators, BlockingQueue<Supervisor> supervisors, BlockingQueue<Director> directors) {

        final int opCap = operators.remainingCapacity();
        final int supCap = supervisors.remainingCapacity();
        final int dirCap = directors.remainingCapacity();

        for (int i = 1; i <= opCap; i++) {
            operators.add(new Operator("Op" + i));
        }
        for (int i = 1; i <= supCap; i++) {
            supervisors.add(new Supervisor("Sup" + i));
        }
        for (int i = 1; i <= dirCap; i++) {
            directors.add(new Director("Dir" + i));
        }
    }

    private List<Call> createCalls(List<Integer> callDurations) {

        final List<Call> calls = new ArrayList<>(callDurations.size());

        for (Integer duration : callDurations) {
            calls.add(new Call(duration));
        }
        return calls;
    }

}