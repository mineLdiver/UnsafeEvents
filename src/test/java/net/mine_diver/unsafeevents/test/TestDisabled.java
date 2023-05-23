package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.MutableEventBus;
import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestDisabled {
    private static final ManagedEventBus MANAGED_EVENT_BUS = new ManagedEventBus();
    public static final MutableEventBus EVENT_BUS = MANAGED_EVENT_BUS;

    public static void main(String[] args) {
        MANAGED_EVENT_BUS.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
        TestListener listener = new TestListener();
        EVENT_BUS.register(Listener.staticMethods().listener(listener.getClass()).build());
        MANAGED_EVENT_BUS.enableDispatch();
        EVENT_BUS.post(TestEvent.builder().stream(System.out).build());
    }
}
