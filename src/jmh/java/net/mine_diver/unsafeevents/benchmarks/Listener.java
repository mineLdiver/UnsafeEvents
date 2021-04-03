package net.mine_diver.unsafeevents.benchmarks;

import net.mine_diver.unsafeevents.listener.EventListener;

public class Listener {

    @EventListener
    public static void listener1(TestEvent event) {
        event.sum += 1;
    }

    @EventListener
    public static void listener2(TestEvent event) {
        event.sum += 2;
    }

    @EventListener
    public static void listener3(TestEvent event) {
        event.sum += 3;
    }
}
