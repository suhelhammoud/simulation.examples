import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class SimParams extends Properties {

    private double holdingCost;
    private double shortageCost;
    private double setupCost;
    private double incrementalCost;
    private int capacity;
    private int threshold;
    private int numMonths;

    private double meanInterDemandTime;
    private double deliveryLagMin;
    private double deliveryLagMax;

    private String outFilePath;

    public static SimParams loadFrom(String fileName)
            throws IOException {
        SimParams result = new SimParams();
        result.load(new FileReader(fileName));
        result.init();
        return result;
    }

    public double getHoldingCost() {
        return holdingCost;
    }

    public double getShortageCost() {
        return shortageCost;
    }

    public double getSetupCost() {
        return setupCost;
    }

    public double getIncrementalCost() {
        return incrementalCost;
    }

    private void init() {

        holdingCost = Double.parseDouble(getProperty("holding.cost"));
        shortageCost = Double.parseDouble(getProperty("shortage.cost"));
        setupCost = Double.parseDouble(getProperty("setup.cost"));
        incrementalCost = Double.parseDouble(getProperty("incremental.cost"));
        capacity = Integer.parseInt(getProperty("capacity"));
        threshold = Integer.parseInt(getProperty("threshold"));
        numMonths = Integer.parseInt(getProperty("num.months"));
        meanInterDemandTime = Double.parseDouble(getProperty("mean.inter.demand.time", "0.1"));
        deliveryLagMin = Double.parseDouble(getProperty("delivery.lag.min", "0.5"));
        deliveryLagMax = Double.parseDouble(getProperty("delivery.lag.max", "1.0"));
        outFilePath = getProperty("out.file.path", "simulation_out_file.txt");

    }

    public double getMeanInterDemandTime() {
        return meanInterDemandTime;
    }

    public double getDeliveryLagMin() {
        return deliveryLagMin;
    }

    public double getDeliveryLagMax() {
        return deliveryLagMax;
    }


    public int getNumMonths() {
        return numMonths;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getOutFilePath() {
        return outFilePath;
    }


}

