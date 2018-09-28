package it.cavallium.warppi.event;

import java.util.List;

public class TouchStartEvent implements TouchEvent {

	private final List<TouchPoint> changedTouches;
	private final List<TouchPoint> touches;

	public TouchStartEvent(final List<TouchPoint> changedTouches, final List<TouchPoint> touches) {
		super();
		this.changedTouches = changedTouches;
		this.touches = touches;
	}

	@Override
	public List<TouchPoint> getChangedTouches() {
		return changedTouches;
	}

	@Override
	public List<TouchPoint> getTouches() {
		return touches;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (changedTouches == null ? 0 : changedTouches.hashCode());
		result = prime * result + (touches == null ? 0 : touches.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TouchStartEvent other = (TouchStartEvent) obj;
		if (changedTouches == null) {
			if (other.changedTouches != null) {
				return false;
			}
		} else if (!changedTouches.equals(other.changedTouches)) {
			return false;
		}
		if (touches == null) {
			if (other.touches != null) {
				return false;
			}
		} else if (!touches.equals(other.touches)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "TouchStartEvent [changedTouches=" + changedTouches + ", touches=" + touches + "]";
	}

}
