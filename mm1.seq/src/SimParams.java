import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class SimParams extends Properties {
    private double meanInterArrivalTime;
    private double meanServiceTime1;
    private double meanServiceTime2;
    private double travelTime;
    private String outFilePath;
    private double endOfSimulation;

    public double getMeanServiceTime2() {
        return meanServiceTime2;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    public double getEndOfSimulation() {
        return endOfSimulation;
    }

    private void init() {
        meanInterArrivalTime = Double.parseDouble(getProperty("mean.arrival.time"));
        meanServiceTime1 = Double.parseDouble(getProperty("mean.service.time1"));
        meanServiceTime2 = Double.parseDouble(getProperty("mean.service.time2"));
        travelTime = Double.parseDouble(getProperty("travel.time"));
        endOfSimulation = Double.parseDouble(getProperty("end.of.simulation"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }


    public double getMeanInterArrivalTime() {
        return meanInterArrivalTime;
    }

    public double getMeanServiceTime1() {
        return meanServiceTime1;
    }


    public String getOutFilePath() {
        return outFilePath;
    }

}

