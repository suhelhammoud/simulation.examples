import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author Suhel Hammoud
 * Reference: A. M. Law, Simulation Modeling & Analysis 3rd edition,
 * Example 1.5 page 60
 */
public class InventorySystem {

    EventList eventList;
    SimRandomGenerator randGen;
    SimStats stats;
    SimParams params;

    int inventoryLevel;

    public void initSimulation(String inputParamsFile) throws IOException {
        /* Initialize time and the event list.  Since no order is outstanding */
        this.eventList = new EventList(0.0);

        this.randGen = new SimRandomGenerator(1);

        /* Initialize the statistical counters. */
        this.stats = new SimStats();

        /* Read input parameters. */
        this.params = SimParams.loadFrom(inputParamsFile);

        /* Initialize the state variables. */
        this.inventoryLevel = params.getCapacity();

        //schedule the first demand event
        double timeNextDemand = randGen.getRandomExponentical(params.getMeanInterDemandTime());
        eventList.add(new SimEvent(EventTag.DEMAND, timeNextDemand));

        //schedule the first evaluation event
        eventList.add(new SimEvent(EventTag.EVALUATE, 1.0));

        //schedule the END_OF_SIMULATION event
        eventList.add(new SimEvent(EventTag.END_OF_SIMULATION, params.getNumMonths()));

    }


    public SimEvent timing() {
        /* Check to see whether the event list is empty. */
        if (eventList.size() == 0) return SimEvent.NONE;
        SimEvent nextEvent = eventList.removeHeadEvent();
        return nextEvent;
    }


    /* Order arrival event method. */
    private void orderArrival(SimEvent ev) {

        /* Increment the inventory level by the amount ordered. */
        int amount = (Integer) ev.data;
        inventoryLevel += amount;
    }

    /* Demand event method. */
    private void demand(SimEvent ev) {
        /* Decrement the inventory level by a generated demand size. */
        int amount = randGen.getRandomDemand();
        inventoryLevel -= amount;

        /* Schedule the time of the next demand. */
        double timeNextDemand = ev.time +
                randGen.getRandomExponentical(params.getMeanInterDemandTime());
        SimEvent nextDemandEvent = new SimEvent(EventTag.DEMAND, timeNextDemand);
        eventList.add(nextDemandEvent);
    }

    /* Inventory-evaluation event method. */
    private void evaluate(SimEvent ev) {
         /* Check whether the inventory level is less than threshold. */

        if (inventoryLevel < params.getThreshold()) {

            /* The inventory level is less than smalls, so place an order for the
                appropriate amount and then update the totalOrderingCost */

            int amount = params.getCapacity() - inventoryLevel;
            double orderingCost = params.getSetupCost() +
                    params.getIncrementalCost() * amount;
            stats.totalOrderingCost += orderingCost;


            /* Schedule the arrival of the order. */
            double timeOrderArrival = ev.time + randGen.getRandomUniform(.5, 1);
            SimEvent nextOrderArrivalEvent = new SimEvent(EventTag.ORDER_ARRIVAL,
                    timeOrderArrival, amount);
            eventList.add(nextOrderArrivalEvent);
        }

        /* Regardless of the place-order decision, schedule the next inventory
            evaluation. */
        eventList.add(new SimEvent(EventTag.EVALUATE, ev.time + 1.0));
    }

    /**
     * Update area accumulators for time-average statistics.
     */
    void updateTimeAvgStats() {

        double timeSinceLastEvent = (eventList.timeSinceLastEvent());

        /*
         Determine the status of the inventory level during the previous interval.
         the inventory level during the previous interval was negative,
         update If it was positive, update area_holding.
         If it was zero, no update is needed.
          */
        if (inventoryLevel < 0) {
            stats.areaShortage -= inventoryLevel * timeSinceLastEvent;
        } else {
            stats.areaHolding += inventoryLevel * timeSinceLastEvent;
        }

    }

    /**
     * Report generator method.
     */
    void report() throws IOException {


        /* Compute and write estimates of desired measures of performance. */
        BufferedWriter outfile = new BufferedWriter(new FileWriter(params.getOutFilePath()));

        /* Write report heading and input parameters. */
        outfile.write("Single-product inventory system\n\n");
        outfile.write(String.format("Initial inventory level%24d items\n\n",
                params.getCapacity()));

        outfile.write(String.format("Mean interdemand time%26.2f\n\n",
                params.getMeanInterDemandTime()));
        outfile.write(String.format("Delivery lag range%29.2f to%10.2f months\n\n",
                params.getDeliveryLagMin(), params.getDeliveryLagMax()));

        outfile.write(stats.printReport(params).toString());
        outfile.close();

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
            if (tag == EventTag.ORDER_ARRIVAL) {
                orderArrival(nextEvent);
                continue;
            }

            if (tag == EventTag.DEMAND) {
                demand(nextEvent);
                continue;
            }

            if (tag == EventTag.EVALUATE) {
                evaluate(nextEvent);
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

        InventorySystem inv = new InventorySystem();
        inv.initSimulation("src/input.params.txt");
        inv.runSimulation();
        System.out.println("Done Simulation");
    }

}
