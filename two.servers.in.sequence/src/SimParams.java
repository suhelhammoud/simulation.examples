import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SimParams extends Properties {
    private double meanInterArrivalTime;
    private double meanServiceTimeA;
    private double meanServiceTimeB;
    private String outFilePath;
    private int customersToEndSimulation;

    public double getMeanServiceTimeB() {
        return meanServiceTimeB;
    }

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    public int getCustomersToEndSimulation() {
        return customersToEndSimulation;
    }

    private void init() {
        meanInterArrivalTime = Double.parseDouble(getProperty("mean.arrival.time"));
        meanServiceTimeA = Double.parseDouble(getProperty("mean.service.time.A"));
        meanServiceTimeB = Double.parseDouble(getProperty("mean.service.time.B"));
        customersToEndSimulation = Integer.parseInt(getProperty("customers.to.End.simulation"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }

    public double getMeanInterArrivalTime() {
        return meanInterArrivalTime;
    }

    public double getMeanServiceTimeA() {
        return meanServiceTimeA;
    }

    public String getOutFilePath() {
        return outFilePath;
    }
}

