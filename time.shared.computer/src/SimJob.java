import java.util.concurrent.atomic.AtomicInteger;

public class SimJob {
    final static AtomicInteger ID = new AtomicInteger();

    final private int id; //unique id per job
    public final double jobAmount;
    public final double submitTime;
    private double jobLeft;


    public SimJob(double submitTime, double jobAmount) {
        id = ID.incrementAndGet();
        this.jobAmount = jobAmount;
        this.submitTime = submitTime;
        this.jobLeft = jobAmount;
    }

    public boolean isComplete() {
        return jobLeft <= 1e-6;
    }

    public double decrease(double quantum) {
        jobLeft -= quantum;
        assert jobLeft >= 0;
        return jobLeft;
    }

    public double getLeft() {
        return jobLeft;
    }

    @Override
    public String toString() {
        return String.format("Job_ %s (sTime: %3.3f, job: %3.3f, left: %3.3f)",
                id, submitTime, jobAmount, jobLeft);
    }

    public static void main(String[] args) {
        SimJob simJob = new SimJob(0, 40);
        System.out.println("simJob = " + simJob);
    }

    @Override
    public boolean equals(Object obj) {
        //efficient code to work only with RoundRobin
        SimJob that = (SimJob) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
