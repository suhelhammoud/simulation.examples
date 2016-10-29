enum EventTag {
    NONE, ORDER_ARRIVAL, DEMAND, EVALUATE, REPORT, END_OF_SIMULATION
}

public class SimEvent implements Comparable<SimEvent> {
    final public static SimEvent NONE = new SimEvent(EventTag.NONE, 0.0);

    public final EventTag tag;
    public final double time;
    public final Object data;

    public SimEvent(EventTag tag, double time, Object data) {
        this.tag = tag;
        this.time = time;
        this.data = data;
    }

    public SimEvent(EventTag tag, double time) {
        this(tag, time, null);
    }

    @Override
    public String toString() {
        return String.format("Event{type: %s, time: %4.3f}", tag, time);
    }



    @Override
    public int compareTo(SimEvent that) {
        return (int) (this.time - that.time);
    }
}
