package net.mine_diver.unsafeevents.benchmarks;

import net.mine_diver.unsafeevents.Event;

public class TestEvent extends Event {

    public int sum;

    public TestEvent(int sum) {
        this.sum = sum;
    }

    @Override
    protected int getEventID() {
        return ID;
    }

    public static final int ID = NEXT_ID.incrementAndGet();
}
