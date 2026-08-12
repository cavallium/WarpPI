package it.cavallium.warppi.desktop;

public class DesktopGpio implements it.cavallium.warppi.Platform.Gpio {

	@Override
	public void initialize() {
	}

	@Override
	public DigitalInput input(final int bcmPin) {
		return () -> false;
	}

	@Override
	public DigitalOutput output(final int bcmPin, final boolean initialHigh) {
		return high -> {
		};
	}

	@Override
	public void delayMicroseconds(final int microseconds) {
	}

	@Override
	public void close() {
	}

}
