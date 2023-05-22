package net.mine_diver.unsafeevents.benchmarks;

import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.MutableEventBus;
import net.mine_diver.unsafeevents.listener.Listener;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 10,
        batchSize = 10_000)
@Fork (1)
@Threads(1)
@State (Scope.Benchmark)
public class Benchmark {
    @Setup
    public void setup() {
        eventBus = new EventBus();
        eventBus.register(Listener.staticMethods().listener(TestListener.class).build());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark(Blackhole blackhole) {
        blackhole.consume(eventBus.post(TestEvent.builder().sum(0).build()).sum);
    }

    public MutableEventBus eventBus;
}
