
public class SimStats {

    public double areaAvailableLines;
    public double totalCallAttempts;

    public double blockedCalls;

    public SimStats() {
        /* Initialize the statistical counters. */
        this.areaAvailableLines = 0;
        this.totalCallAttempts = 0;
        this.blockedCalls = 0;
    }

    /**
     * Compute and write estimates of desired measures of performance.
     *
     * @param params
     * @return
     */
    public String printReport(SimParams params, double simTime) {
        /* Compute and write estimates of desired measures of performance. */
        StringBuilder result = new StringBuilder("");
        result.append(String.format(
                "Time Simulation Ended = %.4f minutes\n", simTime));
        result.append(String.format(
                "Total Call Attempts = %s calls\n", totalCallAttempts));
        result.append(String.format(
                "Total Blocked Calls = %s calls\n", blockedCalls));
        result.append(String.format("Blocked Calls ratio = %.4f \n",
                (double) blockedCalls / totalCallAttempts));
        result.append(String.format(
                "Average Available Lines = %.4f lines\n",
                areaAvailableLines / simTime));
        return result.toString();
    }
}
