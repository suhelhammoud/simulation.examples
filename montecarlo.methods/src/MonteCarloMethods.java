public class MonteCarloMethods {

    //init random generator with different seed each time
    static Random rnd = new Random(System.nanoTime());

    static double f(double x) {
        //change your function formula here
        //return Math.sqrt(x+1);
        return x;
    }

    static double calculatePi(long totalPoints) {
        long inCircle = 0;
        for (long i = 0; i < totalPoints; i++) {
            double x = rnd.nextDouble();
            double y = rnd.nextDouble();
            double d = Math.sqrt(x * x + y * y);

            if (d <= 1.0) inCircle++;
        }
        return 4.0 * (double) inCircle / totalPoints;
    }

    static double calculatePiStream(long totalPoints) {
        long inCircle = LongStream.range(0, totalPoints)
//                .parallel()  // multi-threading stream
                .filter(i -> {
                    double x = rnd.nextDouble();
                    double y = rnd.nextDouble();
                    return Math.sqrt(x * x + y * y) < 1.0;
                }).count();

        return 4.0 * (double) inCircle / totalPoints;
    }

    static double calcFIntegral(double a, double b,
                                int maxY, long totalPoints) {
        //Student should make sure that function f(x)  is valid in range [a,b]
        //Student should make sure that maxY >= maximum value of function f(x)  range [a,b]
        //Make sure that f(x) is positive for all points in range [a,b]
        int underF = 0;

        final double range = b - a;

        for (long i = 0; i < totalPoints; i++) {
            double x = a + rnd.nextDouble() * range; // random value in range [a, b[
            double y = rnd.nextDouble() * maxY;

            if (y <= f(x))
                underF++;
        }
        return (double) underF / totalPoints * range * maxY;
    }

    static double calcFIntegralStream(double a, double b,
                                      int maxY, long totalPoints) {
        final double range = b - a;
        long underF = LongStream.range(0, totalPoints)
//                .parallel()  // multi-threading stream
                .filter(i -> {
                    double x = a + rnd.nextDouble() * range; // random value in range [a, b[
                    double y = rnd.nextDouble() * maxY;
                    return y <= f(x);
                }).count();

        return (double) underF / totalPoints * range * maxY;
    }

    public static void main(String[] args) {

        long totalPoints = 100000000l;

        /* PI */
        double pi = calculatePiStream(totalPoints);
//        double pi = calculatePi(totalPoints);
        System.out.format("Run Pi experiment for %d points, Pi = %f \n",
                totalPoints, pi);

        /* Integration */
        double integral = calcFIntegralStream(0, 1, 10, totalPoints);
//        double integral = calcFIntegral(0, 1, 10, totalPoints);
        System.out.format("Run integral experiment for %d points, integral = %f \n",
                totalPoints, integral);
    }
}

