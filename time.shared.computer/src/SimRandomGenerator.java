import java.util.Random;


public class SimRandomGenerator {
    final private Random rand;

    public SimRandomGenerator(long seed) {
        rand = new Random(seed);
    }

    /**
     * @param mean
     * @return exponential random variable (
     */
    public double expon(double mean) {
        return -mean * Math.log(rand.nextDouble());
    }

}
