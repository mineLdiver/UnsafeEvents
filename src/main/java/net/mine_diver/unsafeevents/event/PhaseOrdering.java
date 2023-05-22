package net.mine_diver.unsafeevents.event;

import it.unimi.dsi.fastutil.objects.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.EventBus;
import net.mine_diver.unsafeevents.listener.GenericListener;
import net.mine_diver.unsafeevents.listener.SingularListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public final class PhaseOrdering<EVENT extends Event> {
    private static final @NotNull Reference2ReferenceMap<@NotNull Class<? extends Event>, @NotNull PhaseOrdering<?>> PHASE_ORDERING_STORAGE = new Reference2ReferenceOpenHashMap<>();
    private static final @NotNull Function<@NotNull Class<? extends Event>, @NotNull PhaseOrdering<? extends Event>> PHASE_ORDERING_GENERATOR = PhaseOrdering::new;

    public static <EVENT extends Event> PhaseOrdering<EVENT> of(final @NotNull Class<EVENT> eventType) {
        //noinspection unchecked
        return (PhaseOrdering<EVENT>) PHASE_ORDERING_STORAGE.computeIfAbsent(eventType, PHASE_ORDERING_GENERATOR);
    }

    Class<EVENT> eventType;

    Object2ReferenceMap<@NotNull String, @Nullable EventPhaseData> phases = new Object2ReferenceOpenHashMap<>();

    List<@NotNull EventPhaseData> sortedPhases = new ReferenceArrayList<>();

    Comparator<SingularListener<?>> listenerComparator = Comparator.<SingularListener<?>, String>comparing(
            GenericListener::phase,
            Comparator.comparingInt(phase -> sortedPhases.indexOf(getOrCreatePhase(phase, true)))
    ).thenComparing(
            Comparator.comparingInt(GenericListener::priority).reversed()
    );

    private PhaseOrdering(final @NotNull Class<EVENT> eventType) {
        this.eventType = eventType;
        if (eventType.isAnnotationPresent(EventPhases.class))
            sortedPhases.addAll(
                    Arrays
                            .stream(eventType.getAnnotation(EventPhases.class).value())
                            .map(phase -> getOrCreatePhase(phase, false))
                            .toList()
            );
    }

    private EventPhaseData getOrCreatePhase(final @NotNull String id, final boolean sortIfCreate) {
        var phase = phases.get(id);

        if (phase == null) {
            phase = new EventPhaseData(id);
            phases.put(id, phase);
            sortedPhases.add(phase);

            if (sortIfCreate) {
                PhaseSorting.sortPhases(sortedPhases);
            }
        }

        return phase;
    }

    public void sort(final @NotNull SingularListener<EVENT> @NotNull [] listeners) {
        Arrays.sort(listeners, listenerComparator);
    }

    public void addPhaseOrdering(final @NotNull String firstPhase, final @NotNull String secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase))
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

        val first = getOrCreatePhase(firstPhase, false);
        val second = getOrCreatePhase(secondPhase, false);
        first.subsequentPhases.add(second);
        second.previousPhases.add(first);
        PhaseSorting.sortPhases(sortedPhases);

        val event = PhaseOrderingInvalidationEvent.INSTANCE;
        event.eventType = eventType;
        EventBus.MANAGEMENT_BUS.post(event);
    }
}
