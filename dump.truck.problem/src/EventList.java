import java.util.PriorityQueue;
import java.util.StringJoiner;

/**
 * Simulation event list implementation using double-linked always sorted queue (PriorityQueue).
 */
public class EventList extends PriorityQueue<SimEvent> {
    private double time;
    private double lastEventTime;


    public EventList(double initTime) {
        /* Initialize the simulation clock. */
        this.time = initTime;
        this.lastEventTime = initTime;
    }

    /**
     * Extract and remove the imminent event from event list. Advance the simulation clock to this event.
     *
     * @return extracted event, or SimEVENT.NONE of event list were empty.
     */
    public SimEvent removeHeadEvent() {
        /* Check to see whether the event list is empty. */
        /* If empty, return NONE event to stop the simulation */
        if (size() == 0) return SimEvent.NONE;

        /* Event list is not empty.*/
        SimEvent ev = super.poll();
        lastEventTime = time;
        time = ev.time;
        return ev;
    }

    /**
     * Read the imminent event without extracting it form the event list
     * @return the head event in the event list
     */
    public SimEvent readHeadEvent() {
        if (size() == 0) return SimEvent.NONE;
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
    public String toString() { // for debugging only
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("Current Event Time= %4.4f", time));
        joiner.add(String.format("Last Evnet Time= %4.4f", lastEventTime));
        joiner.add(String.format("Number of events= %d", size()));
        for (SimEvent ev : this) {
            joiner.add(ev.toString());
        }
        return joiner.toString();
    }
}
