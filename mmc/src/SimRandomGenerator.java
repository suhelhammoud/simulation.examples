import java.util.Random;

public class SimRandomGenerator {

    final private Random rand;

    public SimRandomGenerator(long seed) {
        this.rand = new Random(seed);
    }

    /**
     * Get random sample of uniform distribution between a and b values
     * @param a
     * @param b
     * @return
     */
    public double uniformSample(double a, double b) {
        double range = b - a;
        return a + range * rand.nextDouble();
    }

    /**
     *
     * @param mean
     * @return exponential random variable
     */
    public double exponentialSample(double mean) {
            return -mean * Math.log(rand.nextDouble());
    };

}
