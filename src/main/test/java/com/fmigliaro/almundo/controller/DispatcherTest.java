package com.fmigliaro.almundo.controller;

import com.fmigliaro.almundo.controller.handler.DirectorHandler;
import com.fmigliaro.almundo.controller.handler.EmployeeHandler;
import com.fmigliaro.almundo.controller.handler.OperatorHandler;
import com.fmigliaro.almundo.controller.handler.SupervisorHandler;
import com.fmigliaro.almundo.model.Call;
import com.fmigliaro.almundo.model.Director;
import com.fmigliaro.almundo.model.Operator;
import com.fmigliaro.almundo.model.Supervisor;
import com.fmigliaro.almundo.utility.CallRegistrationAware;
import com.fmigliaro.almundo.utility.CallRegistrationMap;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    /**
     * Este test cubre el caso que enunciado en la consigna del problema.<br/>
     * Se generan 10 llamadas, cada una con una duración aleatoria de entre 5 y 10 segundos. A su vez, se generan<br/>
     * 10 empleados de distinto tipo (4 operadores, 3 supervisores y 3 directores). Dado que todas las tareas se asignan<br/>
     * más rápido que el tiempo mínimo en el que se procesa una tarea (5 segundos), esto implica que el orden de asignación<br/>
     * de llamadas debería ser siempre determinístico, es decir, las primeras 4 llamadas para los Operadores disponibles,<br/>
     * las 3 siguientes para los Supervisores y por último las 3 restantes para los Directores.
     *
     * @throws InterruptedException
     */
    @Test
    public void baseTest() throws InterruptedException {

        final CallRegistrationMap callReg = setupTestRandomDurations(7, 4, 3, 3);

        assertEquals("Operator", callReg.getEmployeeTypeByCallId(1));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(2));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(3));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(4));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(5));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(6));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(7));
        assertEquals("Director", callReg.getEmployeeTypeByCallId(8));
        assertEquals("Director", callReg.getEmployeeTypeByCallId(9));
        assertEquals("Director", callReg.getEmployeeTypeByCallId(10));
    }

    @Test
    public void equalNumOfCallsAndThreadsShouldProcessInCorrectOrder() throws InterruptedException {

        List<Integer> durations = Arrays.asList(2000, 2000, 2000, 2000, 2000, 1000, 1000, 1000, 500, 500);

        final CallRegistrationMap callReg = setupTestDefinedDurations(7, 5, 3, 2,
                durations);

        assertEquals("Operator", callReg.getEmployeeTypeByCallId(1));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(2));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(3));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(4));
        assertEquals("Operator", callReg.getEmployeeTypeByCallId(5));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(6));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(7));
        assertEquals("Supervisor", callReg.getEmployeeTypeByCallId(8));
        assertEquals("Director", callReg.getEmployeeTypeByCallId(9));
        assertEquals("Director", callReg.getEmployeeTypeByCallId(10));

        threadPoolExecutor.shutdown();
        log.info("Thread pool was shut down");
    }

    /**
     * Método auxiliar que se encarga del setup de los tests, creando los objectos necesarios e inyectando las<br/>
     * dependencias requeridas.<br/><br/>
     * El método crea los distintos tipos de empleados, genera las calls (con duración definida o aleatoria),<br/>
     * crea los employee handlers y les inyecta sus dependencias, y construye el Dispatcher inyectándole también las<br/>
     * dependencias correspondientes.
     *
     * @param timeInSecForTestToEnd El tiempo en segundos que se desea que el main thread espere a los demás threads
     *                              antes de finalizar un test.
     * @param totalOps Número total de Operadores que se desea generar.
     * @param totalSups Número total de Supervisores que se desea generar.
     * @param totalDirs Número total de Directores que se desea generar.
     * @param callDurations Lista de duraciones de llamada en millisegundos, para las cuales se crearán las llamadas
     *                      correspondientes. Si se pasa null, significa que las llamadas se generarán con duraciones
     *                      aleatorias de entre 5 y 10 segundos.
     * @return Un objeto que registra la asociación entre llamada y empleado que la atendió.
     * @throws InterruptedException
     */
    private CallRegistrationMap setupTestDefinedDurations(long timeInSecForTestToEnd, int totalOps, int totalSups, int totalDirs,
                                                          List<Integer> callDurations) throws InterruptedException {

        final BlockingQueue<Operator> operators = new ArrayBlockingQueue<>(totalOps);
        final BlockingQueue<Supervisor> supervisors = new ArrayBlockingQueue<>(totalSups);
        final BlockingQueue<Director> directors = new ArrayBlockingQueue<>(totalDirs);

        createEmployees(operators, supervisors, directors);

        final List<Call> calls;
        if (callDurations != null) {
            calls = createCallsWithGivenDurations(callDurations);
        } else {
            calls = createCallsWithRandomDurations(10, 5000, 5000);
        }
        final int timeBeforeRetryMs = 500;
        final EmployeeHandler<Director> dirHandler = DirectorHandler.getInstance(directors, timeBeforeRetryMs);
        final EmployeeHandler<Supervisor> supHandler = SupervisorHandler.getInstance(supervisors, dirHandler);
        final EmployeeHandler<Operator> opHandler = OperatorHandler.getInstance(operators, supHandler);

        final CallRegistrationAware callReg = CallRegistrationMap.getInstance();
        final Dispatcher dispatcher = Dispatcher.getInstance(threadPoolExecutor, opHandler, calls, callReg);

        dispatcher.dispatchCalls();

        TimeUnit.SECONDS.sleep(timeInSecForTestToEnd);

        final CallRegistrationMap callListReg = (CallRegistrationMap) callReg;
        callListReg.printCallToEmployeeMap();

        return callListReg;
    }

    /**
     * Cover method para generar llamadas con duraciones aleatorias.<br/>
     * Ver <code>setupTestDefinedDurations</code>
     *
     * @param timeInSecForTestToEnd El tiempo en segundos que se desea que el main thread espere a los demás threads
     *                              antes de finalizar un test.
     * @param totalOps Número total de Operadores que se desea generar.
     * @param totalSups Número total de Supervisores que se desea generar.
     * @param totalDirs Número total de Directores que se desea generar.
     * @return Un objeto que registra la asociación entre llamada y empleado que la atendió.
     * @throws InterruptedException
     */
    private CallRegistrationMap setupTestRandomDurations(long timeInSecForTestToEnd, int totalOps, int totalSups,
                                                         int totalDirs) throws InterruptedException {
        return setupTestDefinedDurations(timeInSecForTestToEnd, totalOps, totalSups, totalDirs, null);
    }

    private void createEmployees(BlockingQueue<Operator> operators, BlockingQueue<Supervisor> supervisors,
                                 BlockingQueue<Director> directors) {

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

    private List<Call> createCallsWithGivenDurations(List<Integer> callDurations) {

        final List<Call> calls = new ArrayList<>(callDurations.size());

        for (Integer duration : callDurations) {
            calls.add(new Call(duration));
        }
        return calls;
    }

    private List<Call> createCallsWithRandomDurations(int totalCalls, int durationStartOffsetMs, int durationRangeSizeMs) {

        final List<Call> calls = new ArrayList<>(totalCalls);

        for (int i = 0; i < totalCalls; i++) {
            calls.add(new Call(durationStartOffsetMs, durationRangeSizeMs));
        }
        return calls;
    }
}