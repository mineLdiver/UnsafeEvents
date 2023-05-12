package net.mine_diver.unsafeevents.util;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Unsafe util class.
 *
 * @author mine_diver
 */
public class UnsafeProvider {
    /**
     * The unsafe instance obtained using reflection.
     */
    @NotNull
    public static final Unsafe theUnsafe;

    /**
     * IMPL_LOOKUP instance obtained using unsafe.
     */
    @NotNull
    public static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            @NotNull Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = (Unsafe) field.get(null);
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP = (MethodHandles.Lookup) theUnsafe.getObject(theUnsafe.staticFieldBase(field), theUnsafe.staticFieldOffset(field));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
