package it.cavallium.warppi.event;

public interface TouchEventListener {
	default boolean onTouchStart(final TouchStartEvent k) {
		return false;
	}

	default boolean onTouchEnd(final TouchEndEvent k) {
		return false;
	}

	default boolean onTouchCancel(final TouchCancelEvent k) {
		return false;
	}

	default boolean onTouchMove(final TouchMoveEvent k) {
		return false;
	}
}
