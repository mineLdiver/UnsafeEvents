package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.listener.Listener;

public class TestAllListeners {
    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        eventBus.disableDispatch();
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
    }
}
