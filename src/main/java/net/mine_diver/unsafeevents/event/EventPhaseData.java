package net.mine_diver.unsafeevents.event;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EventPhaseData {
	final String id;
	final List<EventPhaseData> subsequentPhases = new ReferenceArrayList<>();
	final List<EventPhaseData> previousPhases = new ReferenceArrayList<>();
	int visitStatus = 0; // 0: not visited, 1: visiting, 2: visited
}