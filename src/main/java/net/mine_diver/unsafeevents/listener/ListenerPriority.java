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

import lombok.RequiredArgsConstructor;

/**
 * Default priorities for event listeners.
 *
 * @author mine_diver
 */
@RequiredArgsConstructor
public enum ListenerPriority {
    /**
     * Listeners with this priority execute first.
     */
    HIGHEST(Integer.MAX_VALUE),

    /**
     * Listeners with this priority execute earlier than normal,
     * but not first.
     */
    HIGH(Integer.MAX_VALUE / 2),

    /**
     * Listeners with this priority execute normally.
     */
    NORMAL(EventListener.DEFAULT_PRIORITY),

    /**
     * Listeners with this priority execute later than normal,
     * but not last.
     */
    LOW(Integer.MIN_VALUE / 2),

    /**
     * Listeners with this priority execute last.
     */
    LOWEST(Integer.MIN_VALUE),

    /**
     * Listeners with this priority can freely set their own numerical priority
     * anywhere between the default ones.
     *
     * @see EventListener#numPriority()
     */
    CUSTOM;

    /**
     * The numerical representation of this priority.
     */
    public final int numPriority;

    /**
     * Whether this priority allows for overrides from {@link EventListener#numPriority()}.
     */
    public final boolean custom;

    /**
     * The custom priority constructor.
     */
    ListenerPriority() {
        this(0, true);
    }

    /**
     * The non-custom priority constructor.
     *
     * @param numPriority the numerical representation of this priority.
     */
    ListenerPriority(
            final int numPriority
    ) {
        this(numPriority, false);
    }
}
