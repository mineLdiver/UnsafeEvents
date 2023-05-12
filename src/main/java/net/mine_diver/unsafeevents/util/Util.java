package net.mine_diver.unsafeevents.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * General utils class.
 *
 * @author mine_diver
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {
    /**
     * Applies an initializer to an object and returns the object.
     *
     * <p>
     *     Used for easier initializing of fields.
     * </p>
     *
     * @param object the object to apply the initializer to.
     * @param initializer the initializer to apply on the object.
     * @return the object
     * @param <T> the object's type.
     */
    @Contract("_, _ -> param1")
    public static <T> @NotNull T make(
            final @NotNull T object,
            final @NotNull Consumer<@NotNull T> initializer
    ) {
        initializer.accept(object);
        return object;
    }
}
