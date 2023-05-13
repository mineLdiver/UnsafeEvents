package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;

public class TestListener {
    static {
        TestDisabled.EVENT_BUS.post(TestEvent.builder().build());
    }

    @EventListener
    private static void listenTest(TestEvent event) {
        System.out.println("Event received: " + event);
    }
}
