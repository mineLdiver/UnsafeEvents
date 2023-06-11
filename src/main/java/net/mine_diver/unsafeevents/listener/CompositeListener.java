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

package net.mine_diver.unsafeevents.listener;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Listener that consists of multiple {@linkplain SingularListener singular listeners}.
 *
 * <p>
 *     Usually used for representing a class or an object
 *     with multiple {@link EventListener} annotated methods.
 * </p>
 *
 * <p>
 *     {@link #phase()} replaces the default
 *     phase for sub-listeners.
 * </p>
 *
 * <p>
 *     {@link #priority()} replaces the default
 *     priority for sub-listeners.
 * </p>
 *
 * @author mine_diver
 */
public interface CompositeListener extends GenericListener {
    /**
     * @return the sub-listeners this listener contains.
     */
    @NotNull ImmutableList<@NotNull SingularListener<?>> subListeners();

    /**
     * Accepts all sub-listeners to a consumer.
     *
     * @param consumer the listener consumer.
     */
    @Override
    default void accept(final @NotNull Consumer<@NotNull SingularListener<?>> consumer) {
        subListeners().forEach(consumer);
    }
}
