
public class SimStats {

    public double totalOrderingCost;
    public double areaHolding;
    public double areaShortage;

    public SimStats() {
            /* Initialize the statistical counters. */
        this.totalOrderingCost = 0;
        this.areaHolding = 0;
        this.areaShortage = 0;
    }

    /**
     * Compute and write estimates of desired measures of performance.
     * @param params
     * @return
     */
    public String printReport(SimParams params) {

        double avgHoldingCost = params.getHoldingCost() *  areaHolding / params.getNumMonths();
        double avgShortageCost = params.getShortageCost() * areaShortage / params.getNumMonths();
        double orderingCost = (double)totalOrderingCost / params.getNumMonths();
        double allCosts = avgHoldingCost + avgShortageCost + orderingCost;
        String result = String.format("\n\ncapacity = %3d \n threshold = %3d \n" +
                        "avg. holding cost  = %15.2f \n" +
                        "avg. shortage cost = %15.2f \n" +
                        "avg. ordering cost = %15.2f \n" +
                        "total costs        = %15.2f",
                params.getCapacity(),params.getThreshold(),
                avgHoldingCost, avgShortageCost, orderingCost, allCosts);
        System.out.println(result);
        return result;
    }

}
