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
public class MMC {

    enum ServerStatus {IDLE, BUSY}

    /**
     * Status Variables
     */
    ServerStatus[] serverStatus;
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
        int numServers = params.getNumServers();
        serverStatus = new ServerStatus[numServers];
        for (int i = 0; i < serverStatus.length; i++) {
            serverStatus[i] = ServerStatus.IDLE;
        }

        queue = new LinkedList<>();

        /* Initialize the simulation clock and event list. */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical variables. */
        this.stats = new SimStats();

        //Schedule First Arrival
        SimEvent firstArrival = new SimEvent(EventTag.ARRIVAL,
                randGen.exponentialSample(params.getMeanInterArrivalTime()));
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


    /**
     * Check if all servers are busy
     * @return false if any was IDLE
     */
    private boolean allServersBusy() {
        for (ServerStatus ss : serverStatus) {
            if(ss == ServerStatus.IDLE) return false;
        }
        return true;
    }

    /**
     * Set the first IDLE server in the servers array to be BUSY
     * @return false if all servers were BUSY
     */
    private boolean setOneMoreBusyServer() {
        //assert allServersBusy() == false;
        for (int i = 0; i < serverStatus.length; i++) {
            if (serverStatus[i] == ServerStatus.IDLE) {
                serverStatus[i] = ServerStatus.BUSY;
                return true;
            }
        }
        return false;
    }

    /**
     * Free the first BUSY server, set it to be IDLE
     * @return false if all servers were idles
     */
    private boolean freeOneMoreServer() {
        for (int i = 0; i < serverStatus.length; i++) {
            if (serverStatus[i] == ServerStatus.BUSY) {
                serverStatus[i] = ServerStatus.IDLE;
                return true;
            }
        }
        return false;
    }

    private void arrive(SimEvent ev) {
        /* Schedule next arrival. */
        double nextArrivalTime = eventList.getTime() +
                randGen.exponentialSample(params.getMeanInterArrivalTime());
        SimEvent nextArrivalEvent = new SimEvent(EventTag.ARRIVAL, nextArrivalTime);
        eventList.add(nextArrivalEvent);

        /* Check to see whether server is busy. */
        if (allServersBusy()) {
            /* Server is busy, so increment number of customers in queue. */
            queue.add(ev.time);
        } else {
            /* At least on of the servers is idle set it to by BUSY*/
            /* Server is idle, so arriving customer has a delay of zero. make the server busy*/
            setOneMoreBusyServer();

            /* Schedule a departure (service completion). */
            double nextDepartureTime = eventList.getTime() +
                    randGen.exponentialSample(params.getMeanServiceTime());
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

        stats.numArrivals++;

        /* Check to see whether the queue is empty. */
        if (queue.size() == 0) {
            /* The queue is empty so make one the server idle */
            freeOneMoreServer();
        } else {
            /* The queue is nonempty, so decrement the number of customers in
                queue. keep the same server busy , no need to change state */
            double headArrivalTime = queue.remove(0); //queue--
            /* Compute the delay of the customer who is beginning service and update
                the total delay accumulator. */
            double delay = nextEvent.time - headArrivalTime;
            stats.totalDelaysInQueue += delay;

            /* schedule departure event for this customer*/
            double nextDepartureTime = nextEvent.time +
                    randGen.exponentialSample(params.getMeanServiceTime());
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


		/* Update area under server-busy indicator function. */
        for (ServerStatus ss : serverStatus) {
            if (ss == ServerStatus.BUSY) {
                stats.areaServerStatus += timeSinceLastEvent;
            }
        }

		/* Update area under number-in-queue function. */
        stats.areaNumInQ += (queue.size() * timeSinceLastEvent);
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {
        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));
        outfile.write("Multiple Servers queueing system\n\n");
        int numServers = params.getNumServers();
        outfile.write(String.format("Number of Servers %11d \n\n", numServers));
        outfile.write(String.format("Mean inter-arrival time%11.3f minutes\n\n", params.getMeanInterArrivalTime()));
        outfile.write(String.format("Mean service time%16.3f minutes\n\n", params.getMeanServiceTime()));
        outfile.write(String.format("End of Simulation %14.2f\n\n", params.getEndOfSimulation()));


        double avgDelayInQueue = stats.totalDelaysInQueue / stats.numArrivals;
        outfile.write(String.format("\n\nAverage delay in queue = %4.3f minutes",
                avgDelayInQueue));

        outfile.write(String.format("\n\nAverage number in queue = %4.3f",
                stats.areaNumInQ / eventList.getTime()));

        outfile.write(String.format("\n\nServer utilization = %4.3f",
                stats.areaServerStatus / eventList.getTime()/ numServers));
        outfile.write(String.format("\n\nTime simulation ended = %4.3f minutes",
                eventList.getTime()));
        outfile.close();
    }

    public void runSimulation() throws IOException {

        double endOfSimulationTime = params.getEndOfSimulation();
        while (eventList.getTime() < endOfSimulationTime) {


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
        MMC mmc = new MMC();
        mmc.initSimulation("src/input.params.txt");
        mmc.runSimulation();
    }

}
