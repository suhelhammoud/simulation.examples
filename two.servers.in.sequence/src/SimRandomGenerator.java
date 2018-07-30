import java.util.Random;

public class SimRandomGenerator {

    final private Random rand;

    public SimRandomGenerator(long seed) {
        this.rand = new Random(seed);
    }

    public boolean isCustomerTypeOne() {
        return rand.nextDouble() < 0.6;
    }

    public boolean willBalkAtQueue(int queueLength) {
        double probability = 1.0 / (1.0 + queueLength);
        return rand.nextDouble() > probability;
    }

    /**
     * @param mean
     * @return exponential random variable
     */
    public double exponentialSample(double mean) {
        return -mean * Math.log(rand.nextDouble());
    }


}
