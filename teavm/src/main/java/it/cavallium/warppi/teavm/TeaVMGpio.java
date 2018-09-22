package it.cavallium.warppi.teavm;

public class TeaVMGpio implements it.cavallium.warppi.Platform.Gpio {

	@Override
	public int valueOutput() {
		return 0;
	}

	@Override
	public int valuePwmOutput() {
		return 0;
	}

	@Override
	public int valueInput() {
		return 0;
	}

	@Override
	public int valueHigh() {
		return 0;
	}

	@Override
	public int valueLow() {
		return 0;
	}

	@Override
	public Object valueUnknownBoardType() {
		return new Object();
	}

	@Override
	public void wiringPiSetupPhys() {}

	@Override
	public void pinMode(final int i, final int type) {}

	@Override
	public void digitalWrite(final int pin, final int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void digitalWrite(final int pin, final boolean val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void pwmWrite(final int pin, final int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void delayMicroseconds(final int t) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public int digitalRead(final int pin) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public Object getBoardType() {
		return new Object();
	}

}
