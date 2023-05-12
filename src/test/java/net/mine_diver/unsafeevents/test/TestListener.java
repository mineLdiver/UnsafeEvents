package net.mine_diver.unsafeevents.test;

import net.mine_diver.unsafeevents.listener.EventListener;

public class TestListener {
    static {
        TestDisabled.EVENT_BUS.post(new TestEvent());
    }

    @EventListener
    private static void listenTest(TestEvent event) {
        System.out.println("Event received: " + event);
    }
}
