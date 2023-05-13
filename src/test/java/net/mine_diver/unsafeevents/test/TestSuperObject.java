package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;

public class TestSuperObject {

    @EventListener
    private void listenForEvent(TestEvent event) {
        event.stream.println("Super object successful");
    }
}
