package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestDisabled {
    public static final EventBus EVENT_BUS = new EventBus();

    public static void main(String[] args) {
        EVENT_BUS.disableDispatch();
        TestListener listener = new TestListener();
        EVENT_BUS.register(Listener.staticMethods().listener(listener.getClass()).build());
        EVENT_BUS.enableDispatch();
        EVENT_BUS.post(TestEvent.builder().stream(System.out).build());
    }
}
