import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Suhel Hammoud
 * Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition, Problem 2.27, page 191
 */
public class TwoServers {

    enum ServerStatus {IDLE, BUSY}

    /**
     * Status Variables
     */
    ServerStatus serverAStatus;
    ServerStatus serverBStatus;

    List<Customer> queueA;
    List<Customer> queueB;

    EventList eventList;
    SimRandomGenerator randGen;
    SimStats stats;
    SimParams params;

    /**
     * Initialize the simulation.
     *
     * @param inputParamsFile
     * @throws IOException
     */
    public void initSimulation(String inputParamsFile) throws IOException {
        /* Read input parameters. */
        this.params = SimParams.loadFrom(inputParamsFile);

        /* Initialize the  state variables. */
        serverAStatus = ServerStatus.IDLE;
        queueA = new LinkedList<>();

        serverBStatus = ServerStatus.IDLE;
        queueB = new LinkedList<>();

        /* Initialize the simulation clock and event list. */
        eventList = new EventList(0.0);

        randGen = new SimRandomGenerator(0L);

        /* Initialize the statistical variables. */
        stats = new SimStats();

        //Schedule First Arrival, put customer instance in data field
        Customer c = new Customer(randGen.isCustomerTypeOne(),
                randGen.exponentialSample(params.getMeanInterArrivalTime()));
        SimEvent firstArrival = new SimEvent(
                EventTag.ARRIVAL_A,
                c.firstArrival,
                c);
        eventList.add(firstArrival);
    }

    /**
     * Extract the next event.
     *
     * @return
     */
    public SimEvent timing() {
        /* Check to see whether the event list is empty. */
        if (eventList.size() == 0) return SimEvent.NONE;
        SimEvent nextEvent = eventList.removeHeadEvent();
        return nextEvent;
    }

    private void arriveA(SimEvent ev) {
        /* Schedule next arrival. */
        Customer nextCustomer = new Customer(randGen.isCustomerTypeOne(),
                ev.time + randGen.exponentialSample(params.getMeanInterArrivalTime()));
        SimEvent nextArrivalEvent = new SimEvent(
                EventTag.ARRIVAL_A,
                nextCustomer.firstArrival,
                nextCustomer);
        eventList.add(nextArrivalEvent);

        Customer c = (Customer) ev.data;
        /* Check balk condition if customer is of type 1*/
        if (c.isTypeOne && randGen.willBalkAtQueue(queueA.size())) {
            //Customer of type one is balking at queueA
            stats.numOfBalks++;
            stats.numServedCustomersOne++;
            return;
        }

        /* Check to see whether server is busy. */
        if (serverAStatus == ServerStatus.BUSY) {
            /* Server is busy, so increment number of customers in queueA. */
            queueA.add(c);
        } else {
            serverAStatus = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double nextDepartureTime = ev.time +
                    randGen.exponentialSample(params.getMeanServiceTimeA());
            SimEvent nextDepartureAEvent = new SimEvent(
                    EventTag.DEPARTURE_A,
                    nextDepartureTime,
                    ev.data); //keep customer in data field
            eventList.add(nextDepartureAEvent);
        }
    }

    /**
     * Departure event function.
     *
     * @param ev
     */
    private void departA(SimEvent ev) {

        /* Check to see whether the queueA is empty. */
        if (queueA.size() == 0) {
            /* The queueA is empty so make the server idle */
            serverAStatus = ServerStatus.IDLE;
        } else {
            /* The queueA is nonempty, so decrement the number of customers in
                queueA. */
            serverAStatus = ServerStatus.BUSY;
            //Serve head customer and schedule its next Departure A
            double nextDepartureTime = ev.time + randGen.exponentialSample(params.getMeanServiceTimeA());
            SimEvent nextDepartureEvent = new SimEvent(EventTag.DEPARTURE_A,
                    nextDepartureTime,
                    queueA.remove(0));
            eventList.add(nextDepartureEvent);
        }

        Customer c = (Customer) ev.data;
        /* Customers type 1 leaves the system here */
        if (c.isTypeOne) {
            //leave the system now
            stats.numServedCustomersOne++;
            stats.totalTimeOne += ev.time - c.firstArrival;
        } else {
            arriveB(ev);
        }

    }

    private void arriveB(SimEvent ev) {
        //Only customers of type 2 will have this method called
        Customer c = (Customer) ev.data;
        /* Check to see whether server is busy. */
        if (serverBStatus == ServerStatus.BUSY) {
            /* Server is busy, so increment number of customers in queueA. */
            queueB.add(c);
        } else {
            /* Server is idle, so arriving customer has a delay of zero. make the server busy*/
            serverBStatus = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double nextDepartureTime = ev.time +
                    randGen.exponentialSample(params.getMeanServiceTimeA());
            SimEvent nextDepartureEvent = new SimEvent(
                    EventTag.DEPARTURE_B,
                    nextDepartureTime,
                    c);
            eventList.add(nextDepartureEvent);
        }

    }

    /**
     * Departure event function.
     *
     * @param ev
     */
    private void departB(SimEvent ev) {
        /* update total number of customers passed the system */
        Customer c = (Customer) ev.data;

        stats.numServedCustomersTwo++;
        stats.totalTimeTwo += ev.time - c.firstArrival;

        /* Check to see whether the queueA is empty. */
        if (queueB.size() == 0) {
            /* The queueA is empty so make the server idle */
            serverBStatus = ServerStatus.IDLE;
        } else {
            /* The queueB is nonempty, so decrement the number of customers in
                queueB. */

            /* schedule departure event for new customer customer*/
            double nextDepartureTime = ev.time +
                    randGen.exponentialSample(params.getMeanServiceTimeA());
            SimEvent nextDepartureEvent = new SimEvent(EventTag.DEPARTURE_B,
                    nextDepartureTime,
                    queueB.remove(0));
            eventList.add(nextDepartureEvent);
        }
    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {

        /* Compute time since last event, and update last-event-time marker. */
        double timeSinceLastEvent = (eventList.timeSinceLastEvent());

        /* Update queue length*/
        stats.setQueueALength(queueA.size());
        stats.setQueueBLength(queueB.size());

        /* Update area under number-in-queueA function. */
        stats.areaServerStatusA += serverAStatus == ServerStatus.BUSY ? timeSinceLastEvent : 0;

        /* Update area under server-busy indicator function. */
        stats.areaNumInQA += (queueA.size() * timeSinceLastEvent);

        /* Update area under number-in-queueB function. */
        stats.areaServerStatusB += serverBStatus == ServerStatus.BUSY ? timeSinceLastEvent : 0;

        /* Update area under server-busy indicator function. */
        stats.areaNumInQB += (queueB.size() * timeSinceLastEvent);
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {

        final double endSimulationTime = eventList.getTime();

        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));
        outfile.write("Two sequential Single-server queueing systems");
        outfile.write(String.format("\n\nMean interarrival time%11.3f minutes", params.getMeanInterArrivalTime()));
        outfile.write(String.format("\n\nMean service1 time%16.3f minutes", params.getMeanServiceTimeA()));
        outfile.write(String.format("\n\nMean service2 time%16.3f minutes", params.getMeanServiceTimeB()));
        outfile.write(String.format("\n\nNumber of Served Customers = %d customers",
                stats.numServedCustomers()));
        outfile.write(String.format("\n\nTotal Simulation Time %11.2f", endSimulationTime));
        outfile.write("\n===================================================================");


        /* Customers type one */
        outfile.write("\n\nCustomers of Type one: ");
        outfile.write(String.format("\n\nNumber of Customers of type one = %d customers",
                stats.numServedCustomersOne));
        outfile.write(String.format("\n\nAverage time spent for customers of type one = %4.3f minutes/customer",
                stats.totalTimeOne / stats.numServedCustomersOne));
        outfile.write(String.format("\n\nNumber of balks for customers of type 1 = %d customers",
                stats.numOfBalks));
        outfile.write("\n===================================================================");


        /* Customers type two */
        outfile.write("\n\nCustomers of Type two: ");
        outfile.write(String.format("\n\nNumber of Customers of type two = %d customers",
                stats.numServedCustomersTwo));

        outfile.write(String.format("\n\nAverage time spent for customers of type two = %4.3f minutes/customer",
                stats.totalTimeTwo / stats.numServedCustomersTwo));
        outfile.write("\n===================================================================");

        /* Calc stats for server A*/
        outfile.write("\n\nServer A : ");
        outfile.write(String.format("\n\nNumber of served customers in Server A = %d",
                stats.numServedCustomers() - stats.numServedCustomersTwo - stats.numOfBalks));
        outfile.write(String.format("\n\nAverage number in queueA = %4.3f",
                stats.areaNumInQA / endSimulationTime));
        outfile.write(String.format("\n\nMax number in queueA = %d",
                stats.getMaxQueueA()));
        outfile.write(String.format("\n\nServer A utilization = %4.3f",
                stats.areaServerStatusA / endSimulationTime));
        outfile.write("\n===================================================================");

        /* Calc stats for server B*/
        outfile.write("\n\nServer B : ");
        outfile.write(String.format("\n\nNumber of served customers in Server B = %d",
                stats.numServedCustomersTwo));
        outfile.write(String.format("\n\nAverage number in queueB = %4.3f",
                stats.areaNumInQB / endSimulationTime));
        outfile.write(String.format("\n\nServer B utilization = %4.3f",
                stats.areaServerStatusB / endSimulationTime));
        outfile.write(String.format("\n\nMax number in queueB = %d",
                stats.getMaxQueueB()));
        outfile.write("\n===================================================================");

        outfile.close();
    }

    public void runSimulation() throws IOException {

        final int customersToEndSimulation = params.getCustomersToEndSimulation();

        while (stats.numServedCustomers() < customersToEndSimulation) {

            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /*  final SimEvent nextEvent = eventList.removeHeadEvent(); */
            final SimEvent ev = timing();

            /* Invoke the appropriate event */
            EventTag tag = ev.tag;
            if (tag == EventTag.ARRIVAL_A) {
                arriveA(ev);
                continue;
            }

            if (tag == EventTag.DEPARTURE_A) {
                departA(ev);
                continue;
            }

            if (tag == EventTag.DEPARTURE_B) {
                departB(ev);
                continue;
            }
        }

        /* Invoke the report generator and end the simulation. */
        report();

    }

    public static void main(String[] args) throws IOException {
        TwoServers twoServers = new TwoServers();
        twoServers.initSimulation("src/input.params.txt");
        twoServers.runSimulation();
    }
}
