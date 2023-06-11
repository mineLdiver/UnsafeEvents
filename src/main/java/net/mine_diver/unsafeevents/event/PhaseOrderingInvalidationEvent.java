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

package net.mine_diver.unsafeevents.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Signals that an event type's phase ordering
 * was changed by a third party and event
 * buses should use the new ordering.
 *
 * @see PhaseOrdering#addPhaseOrdering(String, String)
 * @author mine_diver
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhaseOrderingInvalidationEvent extends Event {
    /**
     * The constant instance of the event.
     */
    static final @NotNull PhaseOrderingInvalidationEvent INSTANCE = new PhaseOrderingInvalidationEvent();

    /**
     * The event ID.
     */
    public static final int ID = nextID();

    /**
     * The event type whose phase ordering was invalidated.
     */
    Class<? extends Event> eventType;

    /**
     * Returns the event type whose
     * event ordering was invalidated.
     *
     * @return the event type whose phase ordering was invalidated.
     */
    public @NotNull Class<? extends Event> getEventType() {
        return Objects.requireNonNull(eventType);
    }

    /**
     * Freeing the event type instance after dispatch.
     */
    @Override
    protected void finish() {
        eventType = null;
    }

    /**
     * Returns the event ID.
     *
     * @return the event ID.
     */
    @Override
    protected int getEventID() {
        return ID;
    }
}
