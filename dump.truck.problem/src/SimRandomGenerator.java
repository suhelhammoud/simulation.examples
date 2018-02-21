import java.util.Random;

public class SimRandomGenerator {

    final private Random rand;

    public SimRandomGenerator(long seed) {
        this.rand = new Random(seed);
    }

    /**
     * Used for truck travel trip duration
     *
     * @param a lower bound of travel time
     * @param b max bound of travel time
     * @return uniform random value in range [a, b]
     */
    public double uniformSample(double a, double b) {
        double range = b - a;
        return a + range * rand.nextDouble();
    }

    /**
     * Used for exponential and poisson random number generators
     * @param mean
     * @return exponential random variable
     */
    public double exponentialSample(double mean) {
        return -mean * Math.log(rand.nextDouble());
    }

}
