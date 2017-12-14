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
import org.junit.After;
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

/**
 * Unit Tests del ejercicio de Al Mundo.
 *
 * Created by Francisco Migliaro on 07/12/2017.
 */
public class DispatcherTest {
    private static final int THREAD_POOL_SIZE = 10;
    private static final int WORK_QUEUE_SIZE = 2;
    private ThreadPoolExecutor threadPoolExecutor;

    @Before
    public void setUp() throws Exception {
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(WORK_QUEUE_SIZE);

        threadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, Long.MAX_VALUE,
                TimeUnit.NANOSECONDS, workQueue, new ThreadPoolExecutor.AbortPolicy());
    }

    @After
    public void tearDown() throws Exception {
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(15, TimeUnit.SECONDS);
    }

    /**
     * <b>Este test no sólo cubre el caso base enunciado en la consigna del problema, sino que además se testea que el<br/>
     * orden de procesamiento de las llamadas atendidas por los empleados sea el correcto</b>, es decir:<br/>
     * Asignar primero a Operadores libres, de no haber, asignar a Supervisores, de no haber, asignar a Directores. Si no<br/>
     * hubiera Directores libres, se decide esperar unos segundos y volver a intentar asignar a Directores hasta que alguno<br/>
     * se libere.<br/>
     * Para desarrollar esta solución, se eligió implementar el patrón de diseño <b>Chain of Responsibility</b>.<br/><br/>
     * El test consiste en lo siguiente:<br/>
     * Se generan 10 llamadas, cada una con una duración aleatoria de entre 5 y 10 segundos. A su vez, se generan<br/>
     * 10 empleados de distinto tipo (4 operadores, 3 supervisores y 3 directores). Dado que todas las tareas se asignan<br/>
     * más rápido (casi inmediatamente) que el tiempo mínimo en el que se procesa una tarea (5 segundos), esto implica<br/>
     * que el orden de asignación de llamadas es determinístico, es decir, las primeras 4 llamadas serán para los<br/>
     * Operadores disponibles, las 3 siguientes para los Supervisores y por último, las 3 restantes para los Directores.
     *
     */
    @Test
    public void baseTest() throws InterruptedException {

        final CallRegistrationMap callReg = setupTestRandomDurations(4, 3,
                3, 10);

        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
    }

    /**
     * <b>Este test cubre la consigna extra que plantea qué pasa con una llamada cuando no hay ningún empleado libre.</b><br/>
     * En ese caso la lógica es la siguiente:<br/><br/>
     * 1) Cada llamada es atendida por el Dispatcher y despachada por el ThreadPoolExecutor, asignando de manera<br/>
     * asíncrona a cada una un thread que se encargará de atenderla.<br/>
     * 2) Dado que el ThreadPoolExecutor se configuró con 10 fixed threads y en el test hay 9 empleados, al llegar la<br/>
     * llamada número 10, el executor creará el thread para atender la llamada.<br/>
     * 3) Luego, el thread intentará primero asignar la llamada a un Operador, pero verá que no hay ningún disponible,<br/>
     * por lo que intentará con un Supervisor. Al no haber Supervisores libres, intentará con un Director y, dado que<br/>
     * no hay Directores libres, el DirectorHandler esperará unos segundos y reiniciará la búsqueda delegándosela al<br/>
     * OperatorHandler quien, en esta oportunidad, tendrá Operadores disponibles y uno de ellos atenderá la llamada.<br/><br/>
     *
     * Para testear este escenario, se setean las primeras 3 llamadas (que deberán se atendidas por Operadores) con la<br/>
     * mitad de duración de las 6 llamadas subsiguientes (que deberán ser atendidas 3 por Supervisores y 3 por<br/>
     * Directores). De esta manera, nos aseguramos que siempre terminen primero los Operadores y sea un Operador el<br/>
     * que atienda la llamada extra al momento de desocuparse antes que el resto.
     *
     */
    @Test
    public void shouldWaitAndProcessProperlyIfNoEmployeesAvailable() throws InterruptedException {

        //Inicializar con 10 llamadas.
        final List<Integer> durations = Arrays.asList(500, 500, 500, 1000, 1000, 1000, 1000, 1000, 1000, 1000);

        //Definir menos empleados que llamadas, en este caso un total de 9.
        final CallRegistrationMap callReg = setupTestDefinedDurations(3, 3,
                3, durations, durations.size());

        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());

        //Un Operador será el que se desocupe primero para atender a la llamada número 10.
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
    }

    /**
     * Este test cubre la consigna extra que plantea qué pasa con una llamada cuando ingresan más de 10 llamadas<br/>
     * concurrentes. En ese caso la lógica es la siguiente:<br/><br/>
     * 1) Cada llamada es atendida por el Dispatcher y despachada por el ThreadPoolExecutor, asignando de manera<br/>
     * asíncrona a cada llamada un thread que se encargará de atenderla.<br/>
     * 2) Dado que el ThreadPoolExecutor se configuró con 10 fixed threads y una work queue de tamaño 2, al llegar la<br/>
     * llamada número 11, el executor almacenará en su work queue interna el Runnable que contiene a la llamada a<br/>
     * procesar.<br/>
     * 3) Repetirá este proceso por cada llamada que no pueda ser inmediatamente atendida, hasta llenar la<br/>
     * work queue. De ocurrir lo anterior, el executor descartará la llamada. Esto permite en un escenario real,<br/>
     * evitar que la cola de mensajes de la cual se consumen las llamadas, crezca demasiado en el caso en que el<br/>
     * procesamiento de las llamadas (consumidor) sea más lento que el arrivo de las mismas (productor).<br/><br/>
     *
     * Para testear este escenario, se setean las primeras 6 llamadas (que deberán se atendidas por Operadores) con la<br/>
     * mitad de duración de las 4 llamadas subsiguientes (que deberán ser atendidas 2 por Supervisores y 2 por<br/>
     * Directores). De esta manera, nos aseguramos que siempre terminen primero los Operadores y sean 2 Operadores los<br/>
     * que atiendan las dos llamadas extra cuando se desocupen.
     *
     */
    @Test
    public void moreCallsThanThreadsWithoutDiscardingCallsShouldProcessInCorrectOrder() throws InterruptedException {

        //Inicializar con 12 llamadas.
        final List<Integer> durations = Arrays.asList(500, 500, 500, 500, 500, 500, 1000, 1000, 1000, 1000, 1000, 1000);

        //Definir menos empleados que llamadas, en este caso un total de 10.
        final CallRegistrationMap callReg = setupTestDefinedDurations(6, 2,
                2, durations, durations.size());

        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Supervisor", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());
        assertEquals("Director", callReg.getEmployeeTypeFromQueue());

        //Las dos llamadas extra
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
        assertEquals("Operator", callReg.getEmployeeTypeFromQueue());
    }

    /**
     * Este test corrobora qué debiera ocurrir cuando: [#llamadas] > [#threads] + [tamaño thread pool work queue].
     * En este caso, [#llamadas=13] > [#threads=10] + [tamaño thread pool work queue=2]. Por lo tanto, lo único que
     * debemos asegurarnos es que el remaining capacity del thread pool work queue sea igual a 1, dado que la política
     * elegida cuando ocurre el caso aquí testeado es que se descarte la llamada que no pudo ser procesada.
     * En un escenario real, el work queue del thread pool se setearía con valores mayores a 3 así puede soportar el
     * caso en donde las llamadas llegan a la cola más rápido de lo que pueden ser procesadas concurrentemente.
     *
     */
    @Test
    public void moreCallsThanThreadsAndWorkingQueueSizeShouldDiscardOneCall() throws InterruptedException {

        //Inicializar con 13 llamadas (una de las llamadas se debería descartar siempre).
        final List<Integer> durations = Arrays.asList(500, 500, 500, 500, 500, 500, 1000, 1000, 1000, 1000, 1000, 1000, 1000);

        //Definir menos empleados que llamadas, en este caso un total de 10.
        final CallRegistrationMap callReg = setupTestDefinedDurations(6, 2,
                2, durations, durations.size());

        assertEquals(1, callReg.getRegSizeRemainingCapacity());
    }

    /**
     * Método auxiliar que se encarga del setup de los tests, creando los objectos necesarios e inyectando las<br/>
     * dependencias requeridas.<br/><br/>
     * El método crea los distintos tipos de empleados, genera las calls (con duración definida o aleatoria),<br/>
     * crea los employee handlers y les inyecta sus dependencias, y construye el Dispatcher inyectándole también las<br/>
     * dependencias correspondientes.
     *
     * @param totalOps Número total de Operadores que se desea generar.
     * @param totalSups Número total de Supervisores que se desea generar.
     * @param totalDirs Número total de Directores que se desea generar.
     * @param callDurations Lista de duraciones de llamada en millisegundos, para las cuales se crearán las llamadas
     *                      correspondientes. Si se pasa null, significa que las llamadas se generarán con duraciones
     *                      aleatorias de entre 5 y 10 segundos.
     * @param callRegSize Tamaño de la cola utilizada para registrar el orden en que los empleados procesan las llamadas.
     * @return Un objeto que registra la asociación entre llamada y empleado que la atendió.
     */
    private CallRegistrationMap setupTestDefinedDurations(int totalOps, int totalSups, int totalDirs,
                                                          List<Integer> callDurations, int callRegSize) throws InterruptedException {

        //Crear las 3 colas de Operadores, Supervisores y Directores.
        final BlockingQueue<Operator> operators = new ArrayBlockingQueue<>(totalOps);
        final BlockingQueue<Supervisor> supervisors = new ArrayBlockingQueue<>(totalSups);
        final BlockingQueue<Director> directors = new ArrayBlockingQueue<>(totalDirs);

        //Generar empleados de cada tipo.
        createEmployees(operators, supervisors, directors);

        final List<Call> calls;
        //Generar las llamadas, ya sea con duraciones aleatorias o preestablecidas.
        if (callDurations != null) {
            calls = createCallsWithGivenDurations(callDurations);
        } else {
            final int totalCalls = 10;
            final int durStartOffsetMs = 5000;
            final int durRangeSizeMs = 5000;

            calls = createCallsWithRandomDurations(totalCalls, durStartOffsetMs, durRangeSizeMs);
        }
        final int retryTimeoutMs = 3000;

        //Crear cada Employee Handler, inyectando las dependencias necesarias.
        final EmployeeHandler<Director> dirHandler = new DirectorHandler(directors, retryTimeoutMs);
        final EmployeeHandler<Supervisor> supHandler = new SupervisorHandler(supervisors, dirHandler);
        final EmployeeHandler<Operator> opHandler = new OperatorHandler(operators, supHandler);

        //Si no hay más empleados disponibles, el Director Handler espera unos segundos y delega la tarea al Operator
        //Handler, reiniciando la búsqueda de empleados disponibles.
        ((DirectorHandler) dirHandler).setSuccessorHandler(opHandler);

        //Instanciar el objeto que va a registrar el orden en qué cada empleado atendió las llamadas.
        final CallRegistrationAware callReg = new CallRegistrationMap(callRegSize);

        //Crear el Dispatcher e inyectarle sus dependencias.
        final Dispatcher dispatcher = new Dispatcher(threadPoolExecutor, opHandler, calls, callReg);
        dispatcher.dispatchCalls();

        //Imprimir el registro de llamadas.
        final CallRegistrationMap callListReg = (CallRegistrationMap) callReg;
        callListReg.printEmployeeCallProcessingOrder();

        return callListReg;
    }

    /**
     * Cover method para generar llamadas con duraciones aleatorias.<br/>
     * Ver <code>setupTestDefinedDurations</code>
     *
     * @param totalOps Número total de Operadores que se desea generar.
     * @param totalSups Número total de Supervisores que se desea generar.
     * @param totalDirs Número total de Directores que se desea generar.
     * @param callRegSize Tamaño de la cola utilizada para registrar el orden en que los empleados procesan las llamadas.
     * @return Un objeto que registra la asociación entre llamada y empleado que la atendió.
     */
    private CallRegistrationMap setupTestRandomDurations(int totalOps, int totalSups,
                                                         int totalDirs, int callRegSize) throws InterruptedException {

        return setupTestDefinedDurations(totalOps, totalSups, totalDirs, null, callRegSize);
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