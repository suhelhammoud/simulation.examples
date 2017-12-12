import java.util.ArrayList;
import java.util.List;

public class RoundRobinQueue<T> {
//IS NOT thread safe

    private double totalQueueLength;
    private int numServedJobs;
    private double lastTime;
    final private List<T> queue;
    private int index;

    public RoundRobinQueue() {
        totalQueueLength = 0;
        numServedJobs = 0;
        lastTime = 0;
        queue = new ArrayList<>();
        index = 0;
    }

    private void update(double time) {
        double duration = time - lastTime;
        totalQueueLength += queue.size() * duration;
        lastTime = time;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public double getTotalQueueLength() {
        return totalQueueLength;
    }

    public int getNumServedJobs() {
        return numServedJobs;
    }

    //TODO ,for testing only, delete when application is ready
    private void add(T job) {
        add(job, 0);
    }

    //TODO ,for testing only, delete when application is ready
    private void remove(T job) {
        remove(job, 0);
    }

    public void add(T job, double lastTime) {
        if (queue.contains(job)) return;
        update(lastTime);
        queue.add(job);
    }

    public void remove(T job, double time) {
        if (queue.isEmpty()) return;
        update(time);
        numServedJobs++;
        queue.remove(job);
        index = queue.size() > 0 ?
                index % queue.size() :
                0;
    }

    public boolean contains(T element) {
        return queue.contains(element);
    }

    public T next() {
        T element = queue.get(index);
        index = (index + 1) % queue.size();
        return element;
    }

    public boolean hasNext() {
        return queue.size() > 0;
    }

    @Override
    public String toString() {
        return String.format("index: %s, size: %s, elements: %s",
                index, queue.size(), queue.toString());
    }

    public static void main(String[] args) {
        //TODO delete later
        RoundRobinQueue<Integer> list = new RoundRobinQueue<>();
//
        list.add(0);
        list.add(2);
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        list.add(1);

        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        list.remove(1);
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
        System.out.println("list.next() = " + list.next());
    }

}

