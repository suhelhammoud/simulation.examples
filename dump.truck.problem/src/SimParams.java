import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Used to read inputs written in "properties" file format
 */
public class SimParams extends Properties {
    private int numTrucks;
    private double lowerTravelTime;
    private double maxTravelTime;
    private double meanLoaderServiceTime;
    private double meanScaleServiceTime;
    private String outFilePath;
    private double endOfSimulationTime;

    private void init() {
        numTrucks = Integer.parseInt(getProperty("num.trucks"));

        lowerTravelTime = Double.parseDouble(getProperty("lower.travel.time"));
        maxTravelTime = Double.parseDouble(getProperty("max.travel.time"));
        meanLoaderServiceTime = Double.parseDouble(getProperty("mean.loader.service.time"));
        meanScaleServiceTime = Double.parseDouble(getProperty("mean.scale.service.time"));
        endOfSimulationTime = Double.parseDouble(getProperty("end.of.simulation.time"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }

    public int getNumTrucks() {
        return numTrucks;
    }

    public double getLowerTravelTime() {
        return lowerTravelTime;
    }

    public double getMaxTravelTime() {
        return maxTravelTime;
    }

    public double getMeanLoaderServiceTime() {
        return meanLoaderServiceTime;
    }

    public double getMeanScaleServiceTime() {
        return meanScaleServiceTime;
    }

    public String getOutFilePath() {
        return outFilePath;
    }

    public double getEndOfSimulationTime() {
        return endOfSimulationTime;
    }

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }
}

