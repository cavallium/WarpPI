package it.cavallium.warppi.event;

import java.util.List;

public interface TouchEvent extends Event {
	List<TouchPoint> getChangedTouches();

	List<TouchPoint> getTouches();
}
