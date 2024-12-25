package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.mine_diver.unsafeevents.listener.Listener;

import java.lang.invoke.MethodHandles;

public class TestStaticMethods {
    static {
        Listener.registerLookup(MethodHandles.lookup());
    }

    @EventListener(phase = "reflection_based")
    private static void listenForTest(TestEvent event) {
        event.stream.println("Static methods successful");
    }
}
