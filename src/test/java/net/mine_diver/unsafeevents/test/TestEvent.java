package net.mine_diver.unsafeevents.test;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import net.mine_diver.unsafeevents.Event;

import java.io.PrintStream;

@SuperBuilder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class TestEvent extends Event {
    PrintStream stream;
}
