package org.warp.picalculator.event;

public class KeyReleasedEvent implements KeyEvent {

	private Key k;

	public KeyReleasedEvent(Key k) {
		this.k = k;
	}

	@Override
	public Key getKey() {
		return k;
	}

}
