import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author Suhel Hammoud
 * Reference: A. M. Law, Simulation Modelling & Analysis 5th edition,
 * Problem number 1.29, page 83
 */
public class TelephonyOfTwoCities {

    EventList eventList;
    SimRandomGenerator randGen;
    SimStats stats;
    SimParams params;

    int availableLines;

    public void initSimulation(String inputParamsFile) throws IOException {
        /* Initialize time and the event list.  Since no order is outstanding */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical counters. */
        this.stats = new SimStats();

        /* Read input parameters. */
        this.params = SimParams.loadFrom(inputParamsFile);

        /* Initialize the state variables. */
        this.availableLines = params.maxAvailableLines;

        //schedule the first call event
        eventList.add(new SimEvent(
                EventTag.CALL_FROM_A,
                randGen.expon(params.meanConnectFromA)
        ));

        eventList.add(new SimEvent(
                EventTag.CALL_FROM_B,
                randGen.expon(params.meanConnectFromB)
        ));

        //schedule the END_OF_SIMULATION event
        eventList.add(new SimEvent(EventTag.END_OF_SIMULATION, params.maxSimulationTime));
    }


    public SimEvent timing() {
        /* Check to see whether the event list is empty. */
        if (eventList.size() == 0) return SimEvent.NONE;
        SimEvent nextEvent = eventList.removeHeadEvent();
        return nextEvent;
    }


    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {
        double timeSinceLastEvent = (eventList.timeSinceLastEvent());
        stats.areaAvailableLines += availableLines * timeSinceLastEvent;
    }

    /**
     * Report generator method.
     */
    void report() throws IOException {

        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.outFilePath));

        /* Write report heading and input parameters. */
        outfile.write("Telephony of Two Cities\n\n");
        outfile.write(String.format("Initial available lines level%24d \n\n",
                params.maxAvailableLines));

        outfile.write(String.format("Mean call interval from A time%26.2f\n\n",
                params.meanConnectFromA));
        outfile.write(String.format("Mean call interval from B time%26.2f\n\n",
                params.meanConnectFromB));
        outfile.write(String.format("Mean call duration time%26.2f\n\n",
                params.meanCallDuration));

        outfile.write(stats.printReport(params, eventList.getTime()).toString());
        outfile.close();
    }

    void connect(SimEvent event) {
        if (event.tag == EventTag.CALL_FROM_A) {
            eventList.add(new SimEvent(
                    EventTag.CALL_FROM_A,
                    eventList.getTime() + randGen.expon(params.meanConnectFromA)
            ));
        } else {
            eventList.add(new SimEvent(
                    EventTag.CALL_FROM_B,
                    eventList.getTime() + randGen.expon(params.meanConnectFromB)
            ));
        }
        stats.totalCallAttempts++;
        if (availableLines > 0) {
            availableLines--;
            eventList.add(new SimEvent(
                    EventTag.CALL_RELEASE,
                    eventList.getTime() + randGen.expon(params.meanCallDuration)
            ));
        } else {
            stats.blockedCalls++;
        }
    }

    void release() {
        availableLines++;
    }

    public void runSimulation() throws IOException {


         /* Run the simulation until it terminates after an END_OF_SIMULATION event
            occurs. */
        while (true) {

            /* Determine the next event. */
            final SimEvent nextEvent = timing();

            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /* Invoke the appropriate event method. */
            EventTag tag = nextEvent.tag;
            if (tag == EventTag.CALL_FROM_A
                    || tag == EventTag.CALL_FROM_B) {
                connect(nextEvent);
                continue;
            }

            if (tag == EventTag.CALL_RELEASE) {
                release();
            }

            /* stop the simulation in case of END_OF_SIMULATION or the event list is empty*/
            if (tag == EventTag.END_OF_SIMULATION ||
                    tag == EventTag.NONE) {

                break;
            }
        }
        report();
    }

    public static void main(String[] args) throws IOException {

        TelephonyOfTwoCities inv = new TelephonyOfTwoCities();
        inv.initSimulation("src/input.params.txt");
        inv.runSimulation();
        System.out.println("Done Simulation");
    }

}
