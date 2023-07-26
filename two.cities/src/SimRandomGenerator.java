import java.util.Random;


public class SimRandomGenerator {

    final private Random rand;

    public SimRandomGenerator(long seed) {
        this.rand = new Random(seed);
    }

    public double uniform(double a, double b) {
        double range = b - a;
        return a + range * rand.nextDouble();
    }

    /**
     * @param mean
     * @return exponential random variable
     */
    public double expon(double mean) {
        return -mean * Math.log(rand.nextDouble());
    }

}
