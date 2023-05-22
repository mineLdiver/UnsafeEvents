package net.mine_diver.unsafeevents.event;

import java.util.*;

class PhaseSorting {
	/**
	 * Deterministically sort a list of phases.
	 * 1) Compute phase SCCs (i.e. cycles).
	 * 2) Sort phases by id within SCCs.
	 * 3) Sort SCCs with respect to each other by respecting constraints, and by id in case of a tie.
	 */
	static void sortPhases(List<EventPhaseData> sortedPhases) {
		// FIRST KOSARAJU SCC VISIT
		List<EventPhaseData> toposort = new ArrayList<>(sortedPhases.size());

		for (EventPhaseData phase : sortedPhases) {
			forwardVisit(phase, null, toposort);
		}

		clearStatus(toposort);
		Collections.reverse(toposort);

		// SECOND KOSARAJU SCC VISIT
		Map<EventPhaseData, PhaseScc> phaseToScc = new IdentityHashMap<>();

		for (EventPhaseData phase : toposort) {
			if (phase.visitStatus == 0) {
				List<EventPhaseData> sccPhases = new ArrayList<>();
				// Collect phases in SCC.
				backwardVisit(phase, sccPhases);
				// Sort phases by id.
				sccPhases.sort(Comparator.comparing(p -> p.id));
				// Mark phases as belonging to this SCC.
				PhaseScc scc = new PhaseScc(sccPhases);

				for (EventPhaseData phaseInScc : sccPhases) {
					phaseToScc.put(phaseInScc, scc);
				}
			}
		}

		clearStatus(toposort);

		// Build SCC graph
		for (PhaseScc scc : phaseToScc.values()) {
			for (EventPhaseData phase : scc.phases) {
				for (EventPhaseData subsequentPhase : phase.subsequentPhases) {
					PhaseScc subsequentScc = phaseToScc.get(subsequentPhase);

					if (subsequentScc != scc) {
						scc.subsequentSccs.add(subsequentScc);
						subsequentScc.inDegree++;
					}
				}
			}
		}

		// Order SCCs according to priorities. When there is a choice, use the SCC with the lowest id.
		// The priority queue contains all SCCs that currently have 0 in-degree.
		PriorityQueue<PhaseScc> pq = new PriorityQueue<>(Comparator.comparing(scc -> scc.phases.get(0).id));
		sortedPhases.clear();

		for (PhaseScc scc : phaseToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				// Prevent adding the same SCC multiple times, as phaseToScc may contain the same value multiple times.
				scc.inDegree = -1;
			}
		}

		while (!pq.isEmpty()) {
			PhaseScc scc = pq.poll();
			sortedPhases.addAll(scc.phases);

			for (PhaseScc subsequentScc : scc.subsequentSccs) {
				subsequentScc.inDegree--;

				if (subsequentScc.inDegree == 0) {
					pq.add(subsequentScc);
				}
			}
		}
	}

	private static void forwardVisit(EventPhaseData phase, EventPhaseData parent, List<EventPhaseData> toposort) {
		if (phase.visitStatus == 0) {
			// Not yet visited.
			phase.visitStatus = 1;

			for (EventPhaseData data : phase.subsequentPhases) {
				forwardVisit(data, phase, toposort);
			}

			toposort.add(phase);
			phase.visitStatus = 2;
		} else if (phase.visitStatus == 1) {
			// Already visiting, so we have found a cycle.
			throw new IllegalStateException(String.format(
					"Event phase ordering conflict detected.%nEvent phase %s is ordered both before and after event phase %s.",
					phase.id, parent.id
			));
		}
	}

	private static void clearStatus(List<EventPhaseData> phases) {
		for (EventPhaseData phase : phases) {
			phase.visitStatus = 0;
		}
	}

	private static void backwardVisit(EventPhaseData phase, List<EventPhaseData> sccPhases) {
		if (phase.visitStatus == 0) {
			phase.visitStatus = 1;
			sccPhases.add(phase);

			for (EventPhaseData data : phase.previousPhases) {
				backwardVisit(data, sccPhases);
			}
		}
	}

	private static class PhaseScc {
		final List<EventPhaseData> phases;
		final List<PhaseScc> subsequentSccs = new ArrayList<>();
		int inDegree = 0;

		private PhaseScc(List<EventPhaseData> phases) {
			this.phases = phases;
		}
	}
}