import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class SimParams extends Properties {
    private int totalNumOfCustomers;
    private double meanInterArrivalTime;
    private double meanServiceTime;
    private String outFilePath;

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private void init() {
        totalNumOfCustomers =
                Integer.parseInt(getProperty("num.customers"));
        meanInterArrivalTime = Double.parseDouble(getProperty("mean.arrival.time"));
        meanServiceTime = Double.parseDouble(getProperty("mean.service.time"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }

    public int getTotalNumOfCustomers() {
        return totalNumOfCustomers;
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

