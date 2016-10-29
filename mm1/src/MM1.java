import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Suhel Hammoud
 *         Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
 *         Example 1.1 page 7
 */
public class MM1 {

    enum ServerStatus {IDLE, BUSY}

    /**
     * Status Variables
     */
    ServerStatus serverStatus;
    List<Double> queue; // record arrival times to calc the delay when starting service on server


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

        /* Initialize the state variables. */
        serverStatus = ServerStatus.IDLE;
        queue = new LinkedList<>();

        /* Initialize the simulation clock and event list. */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical variables. */
        this.stats = new SimStats();

        //Schedule First Arrival
        SimEvent firstArrival = new SimEvent(EventTag.ARRIVAL,
                randGen.getRandomSample(params.getMeanInterArrivalTime()));
        eventList.add(firstArrival);
    }


    /**
     * Etract the next event.
     *
     * @return
     */
    public SimEvent timing() {
        /* Check to see whether the event list is empty. */
        if (eventList.size() == 0) return SimEvent.NONE;
        SimEvent nextEvent = eventList.removeHeadEvent();

        return nextEvent;
    }

    private void arrive(SimEvent ev) {
        /* Schedule next arrival. */
        double nextArrivalTime = eventList.getTime() +
                randGen.getRandomSample(params.getMeanInterArrivalTime());
        SimEvent nextArrivalEvent = new SimEvent(EventTag.ARRIVAL, nextArrivalTime);
        eventList.add(nextArrivalEvent);

        stats.numArrivals++;

        /* Check to see whether server is busy. */
        if (serverStatus == ServerStatus.BUSY) {
            /* Server is busy, so increment number of customers in queue. */
            queue.add(ev.time);
        } else {
            /* Server is idle, so arriving customer has a delay of zero. make the server busy*/
            serverStatus = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double nextDepartureTime = eventList.getTime() +
                    randGen.getRandomSample(params.getMeanServiceTime());
            SimEvent nextDepartureEvent = new SimEvent(EventTag.DEPARTURE,
                    nextDepartureTime);
            eventList.add(nextDepartureEvent);
        }

    }

    /**
     * Departure event function.
     *
     * @param nextEvent
     */
    private void depart(SimEvent nextEvent) {
        /* Check to see whether the queue is empty. */
        if (queue.size() == 0) {
            /* The queue is empty so make the server idle */
            serverStatus = ServerStatus.IDLE;
        } else {
            /* The queue is nonempty, so decrement the number of customers in
                queue. */
            double headArrivalTime = queue.remove(0); //queue--
            /* Compute the delay of the customer who is beginning service and update
                the total delay accumulator. */
            double delay = nextEvent.time - headArrivalTime;
            stats.totalDelaysInQueue += delay;

            /* schedule departure event for this customer*/
            double nextDepartureTime = nextEvent.time +
                    randGen.getRandomSample(params.getMeanServiceTime());
            SimEvent nextDepartureEvent = new SimEvent((EventTag.DEPARTURE), nextDepartureTime);
            eventList.add(nextDepartureEvent);
        }

    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {

	/* Compute time since last event, and update last-event-time marker. */
        double timeSinceLastEvent = (eventList.timeSinceLastEvent());


	/* Update area under number-in-queue function. */
        if (serverStatus == ServerStatus.BUSY)
        	stats.areaNumInQ += (queue.size() * timeSinceLastEvent);


	/* Update area under server-busy indicator function. */
        stats.areaServerStatus += timeSinceLastEvent;
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {
        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));
        outfile.write("Single-server queueing system\n\n");
        outfile.write(String.format("Mean interarrival time%11.3f minutes\n\n", params.getMeanInterArrivalTime()));
        outfile.write(String.format("Mean service time%16.3f minutes\n\n", params.getMeanServiceTime()));
        outfile.write(String.format("Number of customers%14d\n\n", params.getTotalNumOfCustomers()));


        double avgDelayInQueue = stats.totalDelaysInQueue / stats.numArrivals;
        outfile.write(String.format("\n\Average delay in queue = %4.3f minutes",
                avgDelayInQueue));

        outfile.write(String.format("\n\nAverage number in queue = %4.3f",
                stats.areaNumInQ / eventList.getTime()));

        outfile.write(String.format("\n\nServer utilization = %4.3f",
                stats.areaServerStatus / eventList.getTime()));
        outfile.write(String.format("\n\nTime simulation ended = %4.3f minutes",
                eventList.getTime()));
        outfile.close();
    }

    public void runSimulation() throws IOException {

        while (stats.numArrivals < params.getTotalNumOfCustomers()) {


            /*  final SimEvent nextEvent = eventList.removeHeadEvent(); */
            final SimEvent nextEvent = timing();


            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /* Invoke the appropriate event */
            EventTag tag = nextEvent.tag;
            if (tag == EventTag.ARRIVAL) {
                arrive(nextEvent);
                continue;
            }

            if (tag == EventTag.DEPARTURE) {
                depart(nextEvent);
                continue;
            }

            if (tag == EventTag.END_OF_SIMULATION ||
                    tag == EventTag.NONE) {
                break;
            }
        }

        /* Invoke the report generator and end the simulation. */
        report();

    }

    public static void main(String[] args) throws IOException {
        MM1 mm1 = new MM1();
        mm1.initSimulation("src/input.params.txt");
        mm1.runSimulation();
    }

}
