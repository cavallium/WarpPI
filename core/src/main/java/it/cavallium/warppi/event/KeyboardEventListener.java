package it.cavallium.warppi.event;

public interface KeyboardEventListener {
	default boolean onKeyPressed(final KeyPressedEvent k) {
		return false;
	}

	default boolean onKeyReleased(final KeyReleasedEvent k) {
		return false;
	}
}
