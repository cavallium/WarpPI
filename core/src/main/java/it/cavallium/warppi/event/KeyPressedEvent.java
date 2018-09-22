package it.cavallium.warppi.event;

public class KeyPressedEvent implements KeyEvent {

	private final Key k;

	public KeyPressedEvent(final Key k) {
		this.k = k;
	}

	@Override
	public Key getKey() {
		return k;
	}

}
