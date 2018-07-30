public class SimStats {

    //Server A
    public double areaNumInQA;
    public double areaServerStatusA;

    //Server B
    public double areaNumInQB;
    public double areaServerStatusB;

    //Customer Type One
    public int numOfBalks;
    public double totalTimeOne;
    private int maxQueueA;
    public int numServedCustomersOne;

    //Customer Type Two
    public double totalTimeTwo;
    private int maxQueueB;
    public int numServedCustomersTwo;

    public int getMaxQueueA() {
        return maxQueueA;
    }

    public int getMaxQueueB() {
        return maxQueueB;
    }

    public void setQueueALength(int length) {
        maxQueueA = Integer.max(maxQueueA, length);
    }

    public void setQueueBLength(int length) {
        maxQueueB = Integer.max(maxQueueB, length);
    }

    public int numServedCustomers() {
        return numServedCustomersOne + numServedCustomersTwo;
    }

    public SimStats() {
        //server A
        areaNumInQA = 0;
        areaServerStatusA = 0;

        //server B
        areaNumInQB = 0;
        areaServerStatusB = 0;

        //Customer type one
        totalTimeOne = 0;
        maxQueueA = 0;
        numServedCustomersOne = 0;
        numOfBalks = 0;

        //Customer type two
        totalTimeTwo = 0;
        maxQueueB = 0;
        numServedCustomersTwo = 0;
    }
}
