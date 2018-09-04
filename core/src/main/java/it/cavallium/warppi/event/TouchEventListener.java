package it.cavallium.warppi.event;

public interface TouchEventListener {
	public default boolean onTouchStart(TouchStartEvent k) {
		return false;
	}

	public default boolean onTouchEnd(TouchEndEvent k) {
		return false;
	}

	public default boolean onTouchCancel(TouchCancelEvent k) {
		return false;
	}

	public default boolean onTouchMove(TouchMoveEvent k) {
		return false;
	}
}
