package net.mine_diver.unsafeevents.event;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.listener.GenericListener;
import net.mine_diver.unsafeevents.listener.SingularListener;
import net.mine_diver.unsafeevents.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.mine_diver.unsafeevents.event.EventPhases.DEFAULT_PHASE;

@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public final class PhaseOrdering<EVENT extends Event> {
    private static final @NotNull Reference2ReferenceMap<@NotNull Class<? extends Event>, @NotNull PhaseOrdering<?>> PHASE_ORDERING_STORAGE = new Reference2ReferenceOpenHashMap<>();
    private static final @NotNull Function<@NotNull Class<? extends Event>, @NotNull PhaseOrdering<? extends Event>> PHASE_ORDERING_GENERATOR = PhaseOrdering::new;

    private static final Set<PhaseOrderingInvalidationCallback> INVALIDATION_LISTENERS = Util.newWeakSet();

    public static <EVENT extends Event> PhaseOrdering<EVENT> of(final @NotNull Class<EVENT> eventType) {
        //noinspection unchecked
        return (PhaseOrdering<EVENT>) PHASE_ORDERING_STORAGE.computeIfAbsent(eventType, PHASE_ORDERING_GENERATOR);
    }

    public static void addInvalidationCallback(PhaseOrderingInvalidationCallback listener) {
        INVALIDATION_LISTENERS.add(listener);
    }

    private static Comparator<SingularListener<?>> bakeComparator(final @NotNull List<@NotNull EventPhaseData> sortedPhases) {
        val immutablePhases = sortedPhases.stream().map(eventPhaseData -> eventPhaseData.id).collect(ImmutableList.toImmutableList());
        return Comparator.<SingularListener<?>, String>comparing(
                GenericListener::phase,
                Comparator.comparingInt(object -> {
                    val index = immutablePhases.indexOf(object);
                    return index < 0 ? Integer.MAX_VALUE : index;
                })
        ).thenComparing(
                Comparator.comparingInt(GenericListener::priority).reversed()
        );
    }

    public Class<EVENT> eventType;

    Object2ReferenceMap<@NotNull String, @Nullable EventPhaseData> phases = new Object2ReferenceOpenHashMap<>();

    List<@NotNull EventPhaseData> sortedPhases = new ReferenceArrayList<>();

    @NonFinal Comparator<SingularListener<?>> listenerComparator;

    Consumer<PhaseOrderingInvalidationCallback> invalidationCallbackInvoker = callback -> callback.phaseOrderingInvalidated(this);

    private PhaseOrdering(final @NotNull Class<EVENT> eventType) {
        this.eventType = eventType;

        // add phases from the annotation, in order
        var noDefault = true;
        @Nullable String prevPhase = null;
        if (eventType.isAnnotationPresent(EventPhases.class)) {
            for (String phase : eventType.getAnnotation(EventPhases.class).value()) {
                if (noDefault && DEFAULT_PHASE.equals(phase))
                    noDefault = false;
                if (prevPhase == null)
                    getOrCreatePhase(phase);
                else
                    addPhaseOrdering(prevPhase, phase);
                prevPhase = phase;
            }
        }

        if (noDefault)
            if (prevPhase == null)
                getOrCreatePhase(DEFAULT_PHASE);
            else
                addPhaseOrdering(prevPhase, DEFAULT_PHASE);

    }

    public Comparator<SingularListener<?>> getListenerComparator() {
        return Objects.requireNonNullElseGet(listenerComparator, () -> listenerComparator = bakeComparator(sortedPhases));
    }

    private EventPhaseData getOrCreatePhase(final @NotNull String id) {
        var phase = phases.get(id);

        if (phase == null) {
            phase = new EventPhaseData(id);
            phases.put(id, phase);
            sortedPhases.add(phase);
            listenerComparator = null;
        }

        return phase;
    }

    public void addPhaseOrdering(final @NotNull String firstPhase, final @NotNull String secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase))
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

        val first = getOrCreatePhase(firstPhase);
        val second = getOrCreatePhase(secondPhase);
        first.subsequentPhases.add(second);
        second.previousPhases.add(first);
        PhaseSorting.sortPhases(sortedPhases);
        listenerComparator = null;

        INVALIDATION_LISTENERS.forEach(invalidationCallbackInvoker);
    }
}
