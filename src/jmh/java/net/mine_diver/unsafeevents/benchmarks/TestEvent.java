package net.mine_diver.unsafeevents.benchmarks;

import lombok.experimental.SuperBuilder;
import net.mine_diver.unsafeevents.Event;

@SuperBuilder
public class TestEvent extends Event {

    public int sum;

    @Override
    protected int getEventID() {
        return ID;
    }

    public static final int ID = nextID();
}
