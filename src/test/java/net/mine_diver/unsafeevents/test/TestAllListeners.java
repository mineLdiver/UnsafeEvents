package net.mine_diver.unsafeevents.test;

import lombok.val;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.event.PhaseOrdering;
import net.mine_diver.unsafeevents.eventbus.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestAllListeners {
    public static void main(String[] args) {
        val instance = ManagedEventBus.create();
        try (EventBus mutableEventBus = instance.eventBus()) {
            ManagedEventBus.Controller eventBusController = instance.eventBusController();

            eventBusController.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
            mutableEventBus.register(
                    Listener.staticMethods()
                            .listener(TestStaticMethods.class)
                            .build()
            );
            mutableEventBus.register(
                    Listener.object()
                            .listener(new TestObject())
                            .build()
            );
            mutableEventBus.register(
                    Listener.<TestEvent>simple()
                            .listener(TestMethodReference::listenForTest)
                            .build()
            );
            eventBusController.enableDispatch();

            mutableEventBus.post(TestEvent.builder().stream(System.out).build());

            PhaseOrdering.of(TestEvent.class).addPhaseOrdering("reflection_based", "default");

            mutableEventBus.post(TestEvent.builder().stream(System.err).build());
        }
    }
}
