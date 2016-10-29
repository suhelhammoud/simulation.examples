import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class SimParams extends Properties {
    private double meanInterArrivalTime;
    private double meanServiceTime;
    private double endOfSimulation;
    private int numServers;
    private String outFilePath;

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private void init() {
        meanInterArrivalTime = Double.parseDouble(getProperty("mean.arrival.time"));
        meanServiceTime = Double.parseDouble(getProperty("mean.service.time"));
        endOfSimulation = Double.parseDouble(getProperty("end.of.simulation"));
        numServers = Integer.parseInt(getProperty("num.servers"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }


    public double getEndOfSimulation() {
        return endOfSimulation;
    }

    public int getNumServers() {
        return numServers;
    }

    public double getMeanInterArrivalTime() {

        return meanInterArrivalTime;
    }

    public double getMeanServiceTime() {
        return meanServiceTime;
    }


    public String getOutFilePath() {
        return outFilePath;
    }

}

