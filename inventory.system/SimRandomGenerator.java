import java.util.Random;


public class SimRandomGenerator {

    final private Random rand;



    public SimRandomGenerator(long seed) {
        this.rand = new Random(seed);
    }

    public double getRandomUniform(double a, double b) {
        double range = b - a;
        return a + range * rand.nextDouble();
    }

    public int getRandomDemand() {
        int d = rand.nextInt(6);
        switch (d) {
            case 0: return 1;
            case 1:
            case 2: return 2;
            case 3:
            case 4: return 3;
            case 5: return 4;
        }
        return 1;
    }

    /**
     *
     * @param mean
     * @return exponential random variable
     */
    public double getRandomExponentical(double mean) {
            return -mean * Math.log(rand.nextDouble());
    };

    public static void main(String[] args) {

    }
}
