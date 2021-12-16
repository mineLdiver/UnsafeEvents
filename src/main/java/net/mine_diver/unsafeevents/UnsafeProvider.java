package net.mine_diver.unsafeevents;

import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.reflect.*;

public class UnsafeProvider {

    public static final Unsafe theUnsafe;
    public static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = (Unsafe) field.get(null);
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP = (MethodHandles.Lookup) theUnsafe.getObject(theUnsafe.staticFieldBase(field), theUnsafe.staticFieldOffset(field));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
