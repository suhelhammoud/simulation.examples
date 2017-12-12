import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SimParams extends Properties {

    private int numTerminals = 0;
    private double meanThinkTime = 0;
    private double meanServiceTime = 0;
    private double quantumTime = 0;
    private double swapTime = 0;
    private int numJobsRequired = 0;
    private double endSimulationTime = 0;
    private String outFilePath;

    public static SimParams loadFrom(String fileName) throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private void init() {
        numTerminals = Integer.parseInt(getProperty("num.terminals", "30"));
        meanThinkTime = Double.parseDouble(getProperty("mean.think.time", "25"));
        meanServiceTime = Double.parseDouble(getProperty("mean.service.time", "0.8"));
        quantumTime = Double.parseDouble(getProperty("service.quantum.time", "0.1"));
        swapTime = Double.parseDouble(getProperty("swap.time", "0.015"));
        numJobsRequired = Integer.parseInt(getProperty("num.required.jobs", "1000"));
        endSimulationTime = Double.parseDouble(getProperty("end.simulation.time", "3600000"));
        outFilePath = getProperty("out.file.path", "cpu_time_shared_out.txt");
    }

    public int terminals() {
        return numTerminals;
    }

    public double meanThinkTime() {
        return meanThinkTime;
    }

    public double meanServiceTime() {
        return meanServiceTime;
    }

    public double quantum() {
        return quantumTime;
    }

    public double swap() {
        return swapTime;
    }

    public int numJobsRequired() {
        return numJobsRequired;
    }

    public double endOfSimulationTime() {
        return endSimulationTime;
    }

    public String outFilePath() {
        return outFilePath;
    }

}
