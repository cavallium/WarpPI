package org.warp.picalculator.event;

public class KeyPressedEvent implements KeyEvent {

	private Key k;

	public KeyPressedEvent(Key k) {
		this.k = k;
	}

	@Override
	public Key getKey() {
		return k;
	}

}
