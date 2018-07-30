import java.util.PriorityQueue;
import java.util.StringJoiner;

public class EventList extends PriorityQueue<SimEvent> {
    private double time;
    private double lastEventTime;

    public EventList(double initTime) {
        /* Initialize the simulation clock. */
        this.time = initTime;
        this.lastEventTime = initTime;
    }

    public SimEvent removeHeadEvent() {
        /* Check to see whether the event list is empty. */
        /* If empty, return NONE event to stop the simulation */
        if (size() == 0) return SimEvent.NONE;

        /* The event list is not empty, so advance the simulation clock. */
        SimEvent ev = super.poll();
        lastEventTime = time;
        time = ev.time;
        return ev;
    }

    public SimEvent readHeadEvent() {
        if(size() == 0) return SimEvent.NONE;
        SimEvent ev = super.peek();
        return ev;
    }

    public double getTime() {
        return time;
    }

    public double getLastEventTime() {
        return lastEventTime;
    }

    public double timeSinceLastEvent() {
        return time - lastEventTime;
    }

    @Override
    public String toString() {
        //for debugging only
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("Current Event Time= %4.4f", time));
        joiner.add(String.format("Last Evnet Time= %4.4f", lastEventTime));
        joiner.add(String.format("Number of events= %d", size()));
        for(SimEvent ev: this){
            joiner.add(ev.toString());
        }
        return joiner.toString();
    }
}
