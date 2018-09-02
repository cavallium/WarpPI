package it.cavallium.warppi.event;

public interface KeyboardEventListener {
	public default boolean onKeyPressed(KeyPressedEvent k) {
		return false;
	}

	public default boolean onKeyReleased(KeyReleasedEvent k) {
		return false;
	}
}
