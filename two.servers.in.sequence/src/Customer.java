public class Customer {
    final public boolean isTypeOne;
    final public double firstArrival;

    private double totalTimeInSystem;

    public Customer(boolean isTypeOne, double firstArrival) {
        this.isTypeOne = isTypeOne;
        this.firstArrival = firstArrival;
    }
}
