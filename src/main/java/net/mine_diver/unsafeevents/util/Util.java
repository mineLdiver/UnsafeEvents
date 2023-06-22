/*
 * MIT License
 *
 * Copyright (c) 2023 mine_diver
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.mine_diver.unsafeevents.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * General utils class.
 *
 * @author mine_diver
 */
@UtilityClass
public class Util {
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
    public <T> @NotNull T make(
            final @NotNull T object,
            final @NotNull Consumer<@NotNull T> initializer
    ) {
        initializer.accept(object);
        return object;
    }

    /**
     * Creates a new set with weak elements.
     *
     * <p>
     *     Useful for storing listeners without
     *     making them non-GCable.
     * </p>
     *
     * @return a new set with weak elements.
     * @param <E> the type of elements maintained by this set.
     */
    public <E> Set<E> newWeakSet() {
        return Collections.newSetFromMap(new WeakHashMap<>());
    }
}
