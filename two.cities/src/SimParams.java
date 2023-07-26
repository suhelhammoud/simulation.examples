import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SimParams extends Properties {

    double maxSimulationTime;
    int maxAvailableLines;
    double meanConnectFromA;
    double meanConnectFromB;
    double meanCallDuration;
    String outFilePath;

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    private void init() {
        maxSimulationTime = Double.parseDouble(getProperty("max.simulation.time"));
        maxAvailableLines = Integer.parseInt(getProperty("max.available.lines"));
        meanConnectFromA = Double.parseDouble(getProperty("mean.connect.from.a"));
        meanConnectFromB = Double.parseDouble(getProperty("mean.connect.from.b"));
        meanCallDuration = Double.parseDouble(getProperty("mean.call.duration"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");
    }
}

