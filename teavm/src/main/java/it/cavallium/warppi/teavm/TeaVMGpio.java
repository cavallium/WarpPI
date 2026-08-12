package it.cavallium.warppi.teavm;

public class TeaVMGpio implements it.cavallium.warppi.Platform.Gpio {

	@Override
	public void initialize() {
	}

	@Override
	public DigitalInput input(final int bcmPin) {
		throw new UnsupportedOperationException("GPIO is unavailable in JavaScript");
	}

	@Override
	public DigitalOutput output(final int bcmPin, final boolean initialHigh) {
		throw new UnsupportedOperationException("GPIO is unavailable in JavaScript");
	}

	@Override
	public void delayMicroseconds(final int microseconds) {
		throw new UnsupportedOperationException("GPIO is unavailable in JavaScript");
	}

	@Override
	public void close() {
	}

}
