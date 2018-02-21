import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Suhel Hammoud
 * Reference: Jerry Banks et. all, Discrete-Event System Simulation (3rd Edition),
 * Example 3.5 page 80 Dump Truck Problem
 */
public class DumpTruckSimulation {

    enum ServerStatus {IDLE, BUSY}

    /**
     * Status Variables
     */
    ServerStatus loader;
    ServerStatus scale;

    List<Double> loaderQueue; // record arrival times to calc the delay when starting service on the loader
    List<Double> scaleQueue; // record arrival times to calc the delay when starting service on the scale


    /**
     * Event list, Random numbers generator, Statistics, and Input parameters.
     */
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
        loader = ServerStatus.IDLE;
        loaderQueue = new LinkedList<>();

        scale = ServerStatus.IDLE;
        scaleQueue = new LinkedList<>();

        /* Initialize the simulation clock and event list. */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical variables. */
        this.stats = new SimStats();


        /* Schedule first Arrivals for all trucks */
        for (int i = 0; i < params.getNumTrucks(); i++) {
            double travelTime = randGen.uniformSample(params.getLowerTravelTime(), params.getMaxTravelTime());
            SimEvent truckArrival = new SimEvent(EventTag.LOADER_ARRIVAL,
                    travelTime);
            eventList.add(truckArrival);
        }

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

    private void loaderArrive(SimEvent ev) {

        /* Check to see whether loader is busy. */
        if (loader == ServerStatus.BUSY) {
            /* loader is busy, so increment number of customers in the loader queue. */
            loaderQueue.add(ev.time);
        } else {
            /* Loader is idle, so arriving truck has a delay of zero. make the loader busy. */
            loader = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double departureTime = eventList.getTime() +
                    randGen.exponentialSample(params.getMeanLoaderServiceTime());
            SimEvent nextDeparture1Event = new SimEvent(EventTag.LOADER_DEPARTURE,
                    departureTime);
            eventList.add(nextDeparture1Event);
        }
    }


    /**
     * Loader departure event procedure.
     *
     * @param ev
     */
    private void loaderDepart(SimEvent ev) {
        /* Check to see whether loader queue is empty. */
        if (loaderQueue.size() == 0) {
            /* Queue is empty, make the loader idle */
            loader = ServerStatus.IDLE;
        } else {
            /* Queue is nonempty, decrement the number of trucks waiting in loader queue. */
            double headArrivalTime = loaderQueue.remove(0); //queue1--
            /* Compute the delay of the truck who is beginning service and update
                the total delay accumulator. */
            double delay = ev.time - headArrivalTime;
            stats.totalDelaysInLoaderQueue += delay;

            /* schedule departure event for this truck */
            double nextDepartureTime = ev.time +
                    randGen.exponentialSample(params.getMeanLoaderServiceTime());
            SimEvent endLoadingEvent = new SimEvent((EventTag.LOADER_DEPARTURE), nextDepartureTime);
            eventList.add(endLoadingEvent);
        }

        /* schedule scale arrival event if at different time
        double nextScaleArrival = ev.time;
        SimEvent scaleArrivalEvent = new SimEvent(EventTag.SCALE_ARRIVAL, nextScaleArrival);
        eventList.add(scaleArrivalEvent);
        */

        /* Call scaleArrive immediately since no travel time between the loader and the scale */
        scaleArrive(ev);
    }


    private void scaleArrive(SimEvent ev) {

        /* Check to see whether scale is busy. */
        if (scale == ServerStatus.BUSY) {
            /* Scale is busy, so increment number of trucks in the queue. */
            scaleQueue.add(ev.time);
        } else {
            /* Scale is idle, so arriving trucks has a delay of zero. make the loader busy*/
            scale = ServerStatus.BUSY;
            /* Schedule a departure (service completion). */
            double departureTime = eventList.getTime() +
                    randGen.exponentialSample(params.getMeanScaleServiceTime());
            SimEvent departureEvent = new SimEvent(EventTag.SCALE_DEPARTURE,
                    departureTime);
            eventList.add(departureEvent);
        }
    }

    /**
     * Scale departure event procedure.
     *
     * @param ev
     */
    private void scaleDepart(SimEvent ev) {
        /* update total number of time of trucks served by the system (complete one job cycle). */
        stats.numArrivals++;

        if (scaleQueue.size() == 0) {
            /* Scale queue is empty, make the scale idle */
            scale = ServerStatus.IDLE;
        } else {
            /* Queue is nonempty, decrement the number of trucks waiting in
                the queue. */
            double headArrivalTime = scaleQueue.remove(0); //queue--
            /* Compute the delay of the truck entering the scale and update
                the total delay accumulator. */
            double delay = ev.time - headArrivalTime;
            stats.totalDelaysInScaleQueue += delay;

            /* schedule departure event for this truck*/
            double departureTime = ev.time +
                    randGen.exponentialSample(params.getMeanScaleServiceTime());
            SimEvent nextDepartureEvent = new SimEvent((EventTag.SCALE_DEPARTURE), departureTime);
            eventList.add(nextDepartureEvent);
        }

        /* Simulate a truck's travel time and schedule the next load arrival event */
        double travelTime = randGen.uniformSample(params.getLowerTravelTime(), params.getMaxTravelTime());
        SimEvent loaderArriveEvent = new SimEvent(EventTag.LOADER_ARRIVAL,
                ev.time + travelTime);
        eventList.add(loaderArriveEvent);
    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {

        /* Compute time since last event, and update last-event-time marker. */
        double timeSinceLastEvent = (eventList.timeSinceLastEvent());


        /* Update area under number-in-queue for the loader. */
        if (loader == ServerStatus.BUSY)
            stats.areaStatusLoader += timeSinceLastEvent;

        /* Update area under server-busy indicator for the loader. */
        stats.areaNumInQLoader += (loaderQueue.size() * timeSinceLastEvent);

        /* Update area under number-in-queue for the scale. */
        if (scale == ServerStatus.BUSY)
            stats.areaStatusScale += timeSinceLastEvent;

        /* Update area under server-busy for the scale. */
        stats.areaNumInQScale += (scaleQueue.size() * timeSinceLastEvent);
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {
        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));
        outfile.write("Dump Truck System\n\n\n");
        outfile.write("----------------- Inputs --------------------\n\n");

        outfile.write(String.format("Number of trucks = %d \n\n", params.getNumTrucks()));
        outfile.write(String.format("Mean loader service time%11.3f minutes\n\n", params.getMeanLoaderServiceTime()));
        outfile.write(String.format("Mean scale service time%16.3f minutes\n\n", params.getMeanScaleServiceTime()));
        outfile.write(String.format("Lower travel time time%11.3f minutes\n\n", params.getLowerTravelTime()));
        outfile.write(String.format("Max travel time time%11.3f minutes\n\n", params.getMaxTravelTime()));
        outfile.write(String.format("Total Simulation Time %11.2f\n\n", params.getEndOfSimulationTime()));


        outfile.write("\n----------------- Outputs --------------------");

        /* Calc stats for the loader*/
        double avgDelayInLoader = stats.totalDelaysInLoaderQueue / stats.numArrivals;
        outfile.write(String.format("\n\nAverage delay in loader = %4.3f minutes",
                avgDelayInLoader));

        outfile.write(String.format("\n\nAverage number in loader's queue = %4.3f",
                stats.areaNumInQLoader / eventList.getTime()));

        outfile.write(String.format("\n\nLoader utilization = %4.3f",
                stats.areaStatusLoader / eventList.getTime()));

        /* Calc same stats for the scale */
        double avgDelayInScale = stats.totalDelaysInScaleQueue / stats.numArrivals;
        outfile.write(String.format("\n\nAverage delay in scale queue = %4.3f minutes",
                avgDelayInScale));

        outfile.write(String.format("\n\nAverage number in scale = %4.3f",
                stats.areaNumInQScale / eventList.getTime()));

        outfile.write(String.format("\n\nScale utilization = %4.3f",
                stats.areaStatusScale / eventList.getTime()));

        outfile.write(String.format("\n\nTotal number of trucks served= %d trucks", stats.numArrivals));

        outfile.write(String.format("\n\nTime simulation ended = %4.3f minutes",
                eventList.getTime()));
        outfile.close();
    }

    /**
     * Main Simulation Loop
     * @throws IOException
     */
    public void runSimulation() throws IOException {

        double endOfSimulationTime = params.getEndOfSimulationTime();

        while (eventList.getTime() < endOfSimulationTime) {

            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /*  final SimEvent nextEvent = eventList.removeHeadEvent(); */
            final SimEvent ev = timing();

            /* Invoke the appropriate event */
            EventTag tag = ev.tag;
            if (tag == EventTag.LOADER_ARRIVAL) {
                loaderArrive(ev);
                continue;
            }

            if (tag == EventTag.LOADER_DEPARTURE) {
                loaderDepart(ev);
                continue;
            }

//            if (tag == EventTag.SCALE_ARRIVAL) { //same as "LOADER_DEPARTURE" tag
//                scaleArrive(ev);
//                continue;
//            }

            if (tag == EventTag.SCALE_DEPARTURE) {
                scaleDepart(ev);
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
        DumpTruckSimulation dumpTruckSimulation = new DumpTruckSimulation();
        dumpTruckSimulation.initSimulation("src/input.params.txt");
        dumpTruckSimulation.runSimulation();
    }

}
