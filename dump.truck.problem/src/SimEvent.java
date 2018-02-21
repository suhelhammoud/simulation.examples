
enum EventTag {
    NONE,
    LOADER_ARRIVAL,
    LOADER_DEPARTURE,
//    SCALE_ARRIVAL, // Redundant event
    SCALE_DEPARTURE,
    END_OF_SIMULATION
}

public class SimEvent implements Comparable<SimEvent> {
    /* utility event used only for internal use.*/
    final public static SimEvent NONE = new SimEvent(EventTag.NONE, 0.0);

    public final EventTag tag;
    public final double time;
    public final Object data;

    /**
     * SimEvent constructor
     * @param tag type of event
     * @param time time when the event will be triggered
     * @param data payload attached with the event (optional)
     */
    public SimEvent(EventTag tag, double time, Object data) {
        this.tag = tag;
        this.time = time;
        this.data = data;
    }

    /**
     * SimEvent constructor
     * @param tag type of event
     * @param time time when the event will be triggered
     */
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
