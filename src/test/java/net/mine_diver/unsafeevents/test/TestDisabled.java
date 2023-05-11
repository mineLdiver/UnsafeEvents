package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.EventBus;

public class TestDisabled {

    public static final EventBus EVENT_BUS = new EventBus();

    public static void main(String[] args) {
        EVENT_BUS.disableDispatch();
        TestListener listener = new TestListener();
        EVENT_BUS.register(listener.getClass());
        EVENT_BUS.enableDispatch();
        EVENT_BUS.post(new TestEvent());
    }
}
