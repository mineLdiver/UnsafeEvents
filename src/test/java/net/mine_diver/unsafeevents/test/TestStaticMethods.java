package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;

public class TestStaticMethods {

    @EventListener(phase = "reflection_based")
    private static void listenForTest(TestEvent event, String s) {
        event.stream.println("Static methods successful");
    }
}
