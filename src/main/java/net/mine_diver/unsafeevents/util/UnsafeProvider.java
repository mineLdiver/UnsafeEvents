package net.mine_diver.unsafeevents.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;

/**
 * Unsafe util class.
 *
 * @author mine_diver
 */
@UtilityClass
public class UnsafeProvider {
    /**
     * The unsafe instance obtained using reflection.
     */
    public final @NotNull Unsafe theUnsafe;

    /**
     * IMPL_LOOKUP instance obtained using unsafe.
     */
    public final @NotNull MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            var field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = (Unsafe) field.get(null);
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP = (MethodHandles.Lookup) theUnsafe.getObject(theUnsafe.staticFieldBase(field), theUnsafe.staticFieldOffset(field));
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
