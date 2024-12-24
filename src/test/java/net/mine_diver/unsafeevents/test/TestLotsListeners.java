package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestLotsListeners {
    public static void main(String[] args) {
        ManagedEventBus eventBus = new ManagedEventBus();

        eventBus.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
        for (int i = 0; i < 100000; i++) {
            eventBus.register(
                    Listener.<TestEvent>simple()
                            .listener(TestMethodReference::listenForTest)
                            .build()
            );
        }
        eventBus.enableDispatch();

        eventBus.post(TestEvent.builder().stream(System.out).build());
    }
}
