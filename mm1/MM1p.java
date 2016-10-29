import java.io.*;


/**
 *
 * @author Suhel Hammoud 
 * 		   Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
 *         Example 1.1 page 7
 */


public class MM1p {

    final int Q_LIMIT = 100;

    enum ServerStatus {
        IDLE, BUSY
    }

    int next_event_type, num_custs_delayed, num_delays_required, num_events,
            num_in_q;
    ServerStatus server_status;
    double area_num_in_q, area_server_status, mean_interarrival, mean_service,
            sim_time, time_last_event, total_of_delays;

    double[] time_arrival = new double[Q_LIMIT + 1];
    double[] time_next_event = new double[3];

    BufferedReader infile;
    BufferedWriter outfile;

    private void deleteFile(String path){
        try {
            (new File(path)).delete();
        } catch (Exception x) {
            System.err.format("%s: error in deleting ", path);
        }
    }

    public void runSimulation() throws IOException {
	
	    /* Clear previous output file  */
        deleteFile("data/mm1.out");
	    
		/* Open input and output files. */

        infile = new BufferedReader(new FileReader("data/mm1.in"));
        outfile = new BufferedWriter(new FileWriter("data/mm1.out"));

		/* Specify the number of events for the timing function. */

        num_events = 2;

		/* Read input parameters. */
        String[] params = infile.readLine().trim().split("\\s+");
        assert params.length == 3;
        mean_interarrival = Double.valueOf(params[0]);
        mean_service = Double.valueOf(params[1]);
        num_delays_required = Integer.valueOf(params[2]);

		/* Write report heading and input parameters. */
        outfile.write("Single-server queueing system\n\n");
        outfile.write("Mean interarrival time " + mean_interarrival
                + " minutes\n\n");
        outfile.write("Mean service time " + mean_service + " minutes\n\n");
        outfile.write("Number of customers " + num_delays_required + "\n\n");

		/* Initialize the simulation. */

        initialize();

		/* Run the simulation while more delays are still needed. */

        while (num_custs_delayed < num_delays_required) {

			/* Determine the next event. */

            timing();

			/* Update time-average statistical accumulators. */

            update_time_avg_stats();

			/* Invoke the appropriate event function. */

            switch (next_event_type) {
                case 1:
                    arrive();
                    break;
                case 2:
                    depart();
                    break;
            }
        }

		/* Invoke the report generator and end the simulation. */

        report();

        infile.close();
        outfile.close();

    }

    /**
     * Initialization function.
     */
    void initialize() {
		/* Initialize the simulation clock. */

        sim_time = 0.0;

		/* Initialize the state variables. */

        server_status = ServerStatus.IDLE;
        num_in_q = 0;
        time_last_event = 0.0;

		/* Initialize the statistical counters. */

        num_custs_delayed = 0;
        total_of_delays = 0.0;
        area_num_in_q = 0.0;
        area_server_status = 0.0;

		/*
		 * Initialize event list. Since no customers are present, the departure
		 * (service completion) event is eliminated from consideration.
		 */

        time_next_event[1] = sim_time + expon(mean_interarrival);
        time_next_event[2] = Double.MAX_VALUE;
    };

    /**
     * Timing function.
     */
    void timing() throws IOException {
        int i;
        double min_time_next_event = Double.MAX_VALUE;

        next_event_type = 0;

		/* Determine the event type of the next event to occur. */

        for (i = 1; i <= num_events; ++i)
            if (time_next_event[i] < min_time_next_event) {
                min_time_next_event = time_next_event[i];
                next_event_type = i;
            }

		/* Check to see whether the event list is empty. */

        if (next_event_type == 0) {

			/* The event list is empty, so stop the simulation. */
            outfile.write("\nEvent list empty at time " + sim_time);
            System.exit(1);
        }

		/* The event list is not empty, so advance the simulation clock. */

        sim_time = min_time_next_event;

        ;
    }

    /*
     * Arrival event function.
     */
    void arrive() throws IOException {
        double delay;

		/* Schedule next arrival. */

        time_next_event[1] = sim_time + expon(mean_interarrival);

		/* Check to see whether server is busy. */

        if (server_status == ServerStatus.BUSY) {

			/* Server is busy, so increment number of customers in queue. */

            ++num_in_q;

			/* Check to see whether an overflow condition exists. */

            if (num_in_q > Q_LIMIT) {
				/* The queue has overflowed, so stop the simulation. */
                outfile.write("\nOverflow of the array time_arrival at");
                outfile.write(" time " + sim_time);
                System.exit(2);
            }

			/*
			 * There is still room in the queue, so store the time of arrival of
			 * the arriving customer at the (new) end of time_arrival.
			 */

            time_arrival[num_in_q] = sim_time;
        }

        else {

			/*
			 * Server is idle, so arriving customer has a delay of zero. (The
			 * following two statements are for program clarity and do not
			 * affect the results of the simulation.)
			 */

            delay = 0.0;
            total_of_delays += delay;

			/* Increment the number of customers delayed, and make server busy. */

            ++num_custs_delayed;
            server_status = ServerStatus.BUSY;

			/* Schedule a departure (service completion). */

            time_next_event[2] = sim_time + expon(mean_service);
        }
        ;
    }

    /*
     * Departure event function.
     */
    void depart() {
        int i;
        double delay;

		/* Check to see whether the queue is empty. */

        if (num_in_q == 0) {

			/*
			 * The queue is empty so make the server idle and eliminate the
			 * departure (service completion) event from consideration.
			 */

            server_status = ServerStatus.IDLE;
            time_next_event[2] = Double.MAX_VALUE;
        }

        else {

			/*
			 * The queue is nonempty, so decrement the number of customers in
			 * queue.
			 */

            --num_in_q;

			/*
			 * Compute the delay of the customer who is beginning service and
			 * update the total delay accumulator.
			 */

            delay = sim_time - time_arrival[1];
            total_of_delays += delay;

			/*
			 * Increment the number of customers delayed, and schedule
			 * departure.
			 */

            ++num_custs_delayed;
            time_next_event[2] = sim_time + expon(mean_service);

			/* Move each customer in queue (if any) up one place. */

            for (i = 1; i <= num_in_q; ++i)
                time_arrival[i] = time_arrival[i + 1];
        }
    }

    /**
     * Report generator function.
     */
    void report() throws IOException {
		/* Compute and write estimates of desired measures of performance. */

        outfile.write("\n\nAverage delay in queue "
                + (total_of_delays / num_custs_delayed) + " minutes\n\n");

        outfile.write("Average number in queue " + (area_num_in_q / sim_time)
                + "\n\n");
        outfile.write("Server utilization " + (area_server_status / sim_time)
                + "\n\n");
        outfile.write("Time simulation ended " + sim_time + " minutes");
    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void update_time_avg_stats() {
        double time_since_last_event;

		/* Compute time since last event, and update last-event-time marker. */

        time_since_last_event = sim_time - time_last_event;
        time_last_event = sim_time;

		/* Update area under number-in-queue function. */

        area_num_in_q += num_in_q * time_since_last_event;

		/* Update area under server-busy indicator function. */

        area_server_status += server_status.ordinal() * time_since_last_event;
    }

    /**
     * Exponential variate generation function.
     *
     * @param mean
     * @return an exponential random variate with mean (mean).
     */
    static double expon(double mean) {
        return -mean * Math.log(Math.random());
    }

    public static void main(String[] args) {
        try {
            new MM1p().runSimulation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}