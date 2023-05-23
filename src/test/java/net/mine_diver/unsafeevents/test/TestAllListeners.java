package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.event.PhaseOrdering;
import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestAllListeners {
    public static void main(String[] args) {
        try (ManagedEventBus eventBus = new ManagedEventBus()) {

            eventBus.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
            eventBus.register(
                    Listener.staticMethods()
                            .listener(TestStaticMethods.class)
                            .build()
            );
            eventBus.register(
                    Listener.object()
                            .listener(new TestObject())
                            .build()
            );
            eventBus.register(
                    Listener.<TestEvent>simple()
                            .listener(TestMethodReference::listenForTest)
                            .build()
            );
            eventBus.enableDispatch();

            eventBus.post(TestEvent.builder().stream(System.out).build());

            PhaseOrdering.of(TestEvent.class).addPhaseOrdering("reflection_based", "default");

            eventBus.post(TestEvent.builder().stream(System.err).build());
        }
    }
}
