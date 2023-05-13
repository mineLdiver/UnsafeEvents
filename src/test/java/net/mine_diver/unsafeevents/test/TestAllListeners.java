package net.mine_diver.unsafeevents.test;

import lombok.val;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.EventBusController;
import net.mine_diver.unsafeevents.ManagedEventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestAllListeners {
    public static void main(String[] args) {
        val instance = ManagedEventBus.create();
        EventBus eventBus = instance.eventBus();
        EventBusController eventBusController = instance.eventBusController();

        eventBusController.disableDispatch("Listeners mustn't be able to cause an event dispatch during registration.");
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
        eventBusController.enableDispatch();

        eventBus.post(TestEvent.builder().stream(System.out).build());
    }
}
