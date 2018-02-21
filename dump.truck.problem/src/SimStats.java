/**
 * Wrapper to hold statistical variables.
 */
public class SimStats {
    public int numArrivals;

    public double totalDelaysInLoaderQueue;
    public double totalDelaysInScaleQueue;

    public double areaNumInQLoader;
    public double areaStatusLoader;

    public double areaNumInQScale;
    public double areaStatusScale;

    public SimStats() {
        this.numArrivals = 0;
        this.totalDelaysInLoaderQueue = 0;
        this.areaNumInQLoader = 0;
        this.areaStatusLoader = 0;
        this.areaNumInQScale = 0;
        this.areaStatusScale = 0;
    }

}
