

public class SimStats {
    public int numArrivals;

    public double totalDelaysInQueue1;
    public double totalDelaysInQueue2;

    public double areaNumInQ1;
    public double areaServerStatus1;

    public double areaNumInQ2;
    public double areaServerStatus2;

    public SimStats() {
        this.numArrivals = 0;
        this.totalDelaysInQueue1 = 0;
        this.areaNumInQ1 = 0;
        this.areaServerStatus1 = 0;
        this.areaNumInQ2 = 0;
        this.areaServerStatus2 = 0;
    }

}
