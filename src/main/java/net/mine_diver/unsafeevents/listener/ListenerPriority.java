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
