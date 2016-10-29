import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Suhel Hammoud
 *         Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
 *         Problem 1.14, 1.15 page 100
 */
public class MM1Seq {

    enum ServerStatus {IDLE, BUSY}

    /**
     * Status Variables
     */
    ServerStatus server1Status;
    ServerStatus server2Status;

    List<Double> queue1; // record arrival times to calc the delay when starting service on server
    List<Double> queue2; // record arrival times to calc the delay when starting service on server


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
        server1Status = ServerStatus.IDLE;
        queue1 = new LinkedList<>();

        server2Status = ServerStatus.IDLE;
        queue2 = new LinkedList<>();

        /* Initialize the simulation clock and event list. */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical variables. */
        this.stats = new SimStats();

        //Schedule First Arrival
        SimEvent firstArrival = new SimEvent(EventTag.ARRIVAL1,
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

    private void arrive1(SimEvent ev) {
        /* Schedule next arrival. */
        double nextArrivalTime = eventList.getTime() +
                randGen.exponentialSample(params.getMeanInterArrivalTime());
        SimEvent nextArrivalEvent = new SimEvent(EventTag.ARRIVAL1, nextArrivalTime);
        eventList.add(nextArrivalEvent);

        /* Check to see whether server is busy. */
        if (server1Status == ServerStatus.BUSY) {
            /* Server is busy, so increment number of customers in queue1. */
            queue1.add(ev.time);
        } else {
            /* Server is idle, so arriving customer has a delay of zero. make the server busy*/
            server1Status = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double nextDepartureTime = eventList.getTime() +
                    randGen.exponentialSample(params.getMeanServiceTime1());
            SimEvent nextDeparture1Event = new SimEvent(EventTag.DEPARTURE1,
                    nextDepartureTime);
            eventList.add(nextDeparture1Event);
        }

    }


    /**
     * Departure event function.
     *
     * @param ev
     */
    private void depart1(SimEvent ev) {
        /* Check to see whether the queue1 is empty. */
        if (queue1.size() == 0) {
            /* The queue1 is empty so make the server idle */
            server1Status = ServerStatus.IDLE;
        } else {
            /* The queue1 is nonempty, so decrement the number of customers in
                queue1. */
            double headArrivalTime = queue1.remove(0); //queue1--
            /* Compute the delay of the customer who is beginning service and update
                the total delay accumulator. */
            double delay = ev.time - headArrivalTime;
            stats.totalDelaysInQueue1 += delay;

            /* schedule departure event for this customer*/
            double nextDepartureTime = ev.time +
                    randGen.exponentialSample(params.getMeanServiceTime1());
            SimEvent nextDepartureEvent = new SimEvent((EventTag.DEPARTURE1), nextDepartureTime);
            eventList.add(nextDepartureEvent);
        }

        /* schedule arrival2 event to the server 2 after random uniform travel time*/
        double nextArrival2Time = ev.time + randGen.uniformSample(0, params.getTravelTime());
        SimEvent nextArrival2Event = new SimEvent(EventTag.ARRIVAL2, nextArrival2Time);
        eventList.add(nextArrival2Event);

    }

    private void arrive2(SimEvent ev) {

        /* Check to see whether server is busy. */
        if (server2Status == ServerStatus.BUSY) {
            /* Server is busy, so increment number of customers in queue1. */
            queue2.add(ev.time);
        } else {
            /* Server is idle, so arriving customer has a delay of zero. make the server busy*/
            server2Status = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double nextDepartureTime = eventList.getTime() +
                    randGen.exponentialSample(params.getMeanServiceTime1());
            SimEvent nextDepartureEvent = new SimEvent(EventTag.DEPARTURE2,
                    nextDepartureTime);
            eventList.add(nextDepartureEvent);
        }

    }

    /**
     * Departure event function.
     *
     * @param nextEvent
     */
    private void depart2(SimEvent nextEvent) {
        /* update total number of customers passed the system */
        stats.numArrivals++;

        /* Check to see whether the queue1 is empty. */
        if (queue2.size() == 0) {
            /* The queue1 is empty so make the server idle */
            server2Status = ServerStatus.IDLE;
        } else {
            /* The queue1 is nonempty, so decrement the number of customers in
                queue1. */
            double headArrivalTime = queue2.remove(0); //queue1--
            /* Compute the delay of the customer who is beginning service and update
                the total delay accumulator. */
            double delay = nextEvent.time - headArrivalTime;
            stats.totalDelaysInQueue2 += delay;

            /* schedule departure event for this customer*/
            double nextDepartureTime = nextEvent.time +
                    randGen.exponentialSample(params.getMeanServiceTime1());
            SimEvent nextDepartureEvent = new SimEvent((EventTag.DEPARTURE2), nextDepartureTime);
            eventList.add(nextDepartureEvent);
        }

    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {

		/* Compute time since last event, and update last-event-time marker. */
        double timeSinceLastEvent = (eventList.timeSinceLastEvent());


		/* Update area under number-in-queue1 function. */
        if (server1Status == ServerStatus.BUSY)
            stats.areaServerStatus1 += timeSinceLastEvent;

		/* Update area under server-busy indicator function. */
        stats.areaNumInQ1 += (queue1.size() * timeSinceLastEvent);

		/* Update area under number-in-queue2 function. */
        if (server2Status == ServerStatus.BUSY)
            stats.areaServerStatus2 += timeSinceLastEvent;

		/* Update area under server-busy indicator function. */
        stats.areaNumInQ2 += (queue2.size() * timeSinceLastEvent);
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {
        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));
        outfile.write("Two sequential Single-server queueing systems\n\n");
        outfile.write(String.format("Mean interarrival time%11.3f minutes\n\n", params.getMeanInterArrivalTime()));
        outfile.write(String.format("Mean service1 time%16.3f minutes\n\n", params.getMeanServiceTime1()));
        outfile.write(String.format("Max travel time time%11.3f minutes\n\n", params.getTravelTime()));
        outfile.write(String.format("Mean service2 time%16.3f minutes\n\n", params.getMeanServiceTime2()));
        outfile.write(String.format("Total Simulation Time %11.2f\n\n", params.getEndOfSimulation()));


        double avgDelayInQueue1 = stats.totalDelaysInQueue1 / stats.numArrivals;
        outfile.write(String.format("\n\nAveray delay in queue1 = %4.3f minutes",
                avgDelayInQueue1));

        outfile.write(String.format("\n\nAverage number in queue1 = %4.3f",
                stats.areaNumInQ1 / eventList.getTime()));

        outfile.write(String.format("\n\nServer1 utilization = %4.3f",
                stats.areaServerStatus1 / eventList.getTime()));

        /* Calc same stats for server two */
         double avgDelayInQueue2 = stats.totalDelaysInQueue2 / stats.numArrivals;
        outfile.write(String.format("\n\nAveray delay in queue2 = %4.3f minutes",
                avgDelayInQueue2));

        outfile.write(String.format("\n\nAverage number in queue2 = %4.3f",
                stats.areaNumInQ2 / eventList.getTime()));

        outfile.write(String.format("\n\nServer2 utilization = %4.3f",
                stats.areaServerStatus2 / eventList.getTime()));

        outfile.write(String.format("\n\nTime simulation ended = %4.3f minutes",
                eventList.getTime()));
        outfile.close();
    }

    public void runSimulation() throws IOException {

        double endOfSimulationTime = params.getEndOfSimulation();

        while (eventList.getTime() < endOfSimulationTime) {

            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /*  final SimEvent nextEvent = eventList.removeHeadEvent(); */
            final SimEvent ev = timing();

            /* Invoke the appropriate event */
            EventTag tag = ev.tag;
            if (tag == EventTag.ARRIVAL1) {
                arrive1(ev);
                continue;
            }

            if (tag == EventTag.DEPARTURE1) {
                depart1(ev);
                continue;
            }

            if (tag == EventTag.ARRIVAL2) {
                arrive2(ev);
                continue;
            }

            if (tag == EventTag.DEPARTURE2) {
                depart2(ev);
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
        MM1Seq mm1Seq = new MM1Seq();
        mm1Seq.initSimulation("src/input.params.txt");
        mm1Seq.runSimulation();
    }

}
