package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;

public class TestObject extends TestSuperObject {

    @EventListener(phase = "reflection_based")
    private void listenForTest(TestEvent event) {
        event.stream.println("Object successful");
    }
}
