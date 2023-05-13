package net.mine_diver.unsafeevents.test;

public class TestMethodReference {

    public static void listenForTest(TestEvent event) {
        event.stream.println("Method reference successful");
    }
}
