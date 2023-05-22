package net.mine_diver.unsafeevents.test;

import lombok.val;
import net.mine_diver.unsafeevents.MutableEventBus;
import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestDisabled {
    public static final MutableEventBus EVENT_BUS;
    private static final ManagedEventBus.Controller EVENT_BUS_CONTROLLER;
    static {
        val instance = ManagedEventBus.create();
        EVENT_BUS = instance.eventBus();
        EVENT_BUS_CONTROLLER = instance.eventBusController();
    }

    public static void main(String[] args) {
        EVENT_BUS_CONTROLLER.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
        TestListener listener = new TestListener();
        EVENT_BUS.register(Listener.staticMethods().listener(listener.getClass()).build());
        EVENT_BUS_CONTROLLER.enableDispatch();
        EVENT_BUS.post(TestEvent.builder().stream(System.out).build());
    }
}
