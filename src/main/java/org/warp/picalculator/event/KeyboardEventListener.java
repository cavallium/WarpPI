package org.warp.picalculator.event;

public interface KeyboardEventListener {
	public default boolean onKeyPressed(KeyPressedEvent k) {
		return false;
	}

	public default boolean onKeyReleased(KeyReleasedEvent k) {
		return false;
	}
}
