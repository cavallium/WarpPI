package it.cavallium.warppi.event;

import java.util.List;

public interface TouchEvent extends Event {
	public List<TouchPoint> getChangedTouches();

	public List<TouchPoint> getTouches();
}
