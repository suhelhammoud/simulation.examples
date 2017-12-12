import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;

/**
 * @author Suhel Hammoud
 * Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
 * Example 2.5 page 129 Time-Shared Computer Model
 */
public class TimeSharedComputer {

    EventList eventList;
    SimRandomGenerator randGen;
    SimStats stats;
    SimParams params;
    boolean cpuIsIdle = true;

    RoundRobinQueue<SimJob> jobQueue;

    public void initSimulation(String inputParamsFile) throws IOException {
        /* Initialize time and the event list.  Since no order is outstanding */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical counters. */
        this.stats = new SimStats();

        /* Read input parameters. */
        this.params = SimParams.loadFrom(inputParamsFile);

        /* Initialize the state variables. */
        this.jobQueue = new RoundRobinQueue<>();


        //schedule the first jobs submitted by all terminals
        for (int i = 0; i < params.terminals(); i++) {
            SimJob job = thinkAndThenGenerateJob(eventList.getTime());
            SimEvent jobEvent = new SimEvent(EventTag.JOB_ARRIVAL, job.submitTime, job);
            assert jobEvent.time >= eventList.getTime();
            eventList.add(jobEvent);
        }

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
        stats.totalServerUtilization += cpuIsIdle ? 0 : eventList.timeSinceLastEvent();
        stats.totalJobsInQueue +=
                eventList.timeSinceLastEvent() * jobQueue.size();
    }

    /**
     * Report generator method.
     */
    public String report() throws IOException {
        final double simulationTime = eventList.getTime();
        StringJoiner result = new StringJoiner("\n");
        result.add("Time-shared Computer Model");
        result.add(String.format("Number of terminals %9d", params.terminals()));
        result.add(String.format("Mean think time %11.3f seconds", params.meanThinkTime()));
        result.add(String.format("Mean service time %11.3f seconds", params.meanServiceTime()));
        result.add(String.format("Quantum %11.3f seconds", params.quantum()));
        result.add(String.format("Swap time %11.3f seconds", params.swap()));
        result.add(String.format("Number of jobs processed %12d", params.numJobsRequired()));
        result.add(String.format("Average TimeSharedComputer utilization %11.3f", stats.totalServerUtilization / simulationTime));
        result.add(String.format("Average response time %11.3f", stats.totalResponseTime / stats.numCompletedJobs));
        result.add(String.format("Average number in queue %11.3f", stats.totalJobsInQueue / simulationTime));
        result.add(String.format("jobqueue response time %11.3f", jobQueue.getTotalQueueLength() / stats.numCompletedJobs));//debug
        result.add(String.format("jobqueue number in queue %11.3f", jobQueue.getTotalQueueLength() / simulationTime));//debug
        /* Compute and write estimates of desired measures of performance. */

        String toPrint = result.toString();
        Files.write(Paths.get(params.outFilePath()), result.toString().getBytes());
        return toPrint;
    }


    public void runSimulation() throws IOException {

         /* Run the simulation until it terminates after an END_OF_SIMULATION event
            occurs. */
        while (stats.numCompletedJobs < params.numJobsRequired()) {
            assert eventList.size() > 0;

            /* Determine the next event. */
            final SimEvent nextEvent = timing();
            System.out.println("nextEvent = " + nextEvent.tag);

            /* Update time-average statistical accumulators. */
            updateTimeAvgStats();

            /* Invoke the appropriate event method. */
            EventTag tag = nextEvent.tag;
            if (tag == EventTag.JOB_ARRIVAL) {
                arrive(nextEvent);
                continue;
            }

            if (tag == EventTag.END_CPU_RUN) {
                endCpuRun(nextEvent);
                continue;
            }

            /* stop the simulation in case of END_OF_SIMULATION or the event list is empty*/
            if (tag == EventTag.END_OF_SIMULATION ||
                    tag == EventTag.NONE) {
                report();
                break;
            }

            System.err.println("Never reached!");
        }

        System.out.println(report());
    }

    private void startCpuRun(SimJob job, double time) {
        if (job.isComplete()) {
            System.out.println("job = " + job);
        }
        assert !job.isComplete();
        cpuIsIdle = false;
        double timeToRun = Math.min(job.getLeft(), params.quantum());
        job.decrease(timeToRun);
        eventList.add(new SimEvent(EventTag.END_CPU_RUN,
                time + timeToRun + params.swap(),
                job));
    }

    public SimJob thinkAndThenGenerateJob(double time) {
        double submitTime = time + randGen.expon(params.meanThinkTime());
        double serviceTime = 0;
        while (serviceTime < 1e-10) {
            serviceTime = randGen.expon(params.meanServiceTime());
        }
        assert serviceTime > 0; //TODO delete later
        return new SimJob(submitTime, serviceTime);
    }

    private void endCpuRun(SimEvent event) {//one cpu tick
        SimJob runJob = (SimJob) event.data;
        assert jobQueue.contains(runJob);
        if (runJob.isComplete()) {
            jobQueue.remove(runJob, event.time);

            //Update Stats
            stats.numCompletedJobs++;
            final double responseTime = event.time - runJob.submitTime;
            stats.totalResponseTime += responseTime;

            cpuIsIdle = jobQueue.isEmpty();

            //Check end of simulation
            if (stats.numCompletedJobs > params.numJobsRequired()) {
                eventList.add(new SimEvent(EventTag.END_OF_SIMULATION, event.time));
                return;
            }

            //Send it back to terminal, so the terminal will think for a time and then submit a new Job
            SimJob nextJob = thinkAndThenGenerateJob(event.time);
            eventList.add(new SimEvent(EventTag.JOB_ARRIVAL, nextJob.submitTime, nextJob));

        } else if (jobQueue.size() != 0) { //Job is not complete get the next job in the jobqueueu
            startCpuRun(jobQueue.next(), event.time);
        }

    }

    private void arrive(SimEvent jobEvent) {
        SimJob job = (SimJob) jobEvent.data;
        assert job.submitTime - eventList.getTime() < Double.MIN_VALUE;

        /* Place the arriving job at the end of the TimeSharedComputer queue.
         * Stored for each job record  */
        jobQueue.add(job, jobEvent.time);

        startCpuRun(job, jobEvent.time);
    }

    public static void main(String[] args) throws IOException {

        TimeSharedComputer timeSharedModle = new TimeSharedComputer();
        timeSharedModle.initSimulation("src/input.params.txt");
        timeSharedModle.runSimulation();
        System.out.println("Done Simulation");
    }

}
