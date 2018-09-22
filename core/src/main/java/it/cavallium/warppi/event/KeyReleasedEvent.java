package it.cavallium.warppi.event;

public class KeyReleasedEvent implements KeyEvent {

	private final Key k;

	public KeyReleasedEvent(final Key k) {
		this.k = k;
	}

	@Override
	public Key getKey() {
		return k;
	}

}
