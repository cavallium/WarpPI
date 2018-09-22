package it.cavallium.warppi.desktop;

public class DesktopGpio implements it.cavallium.warppi.Platform.Gpio {

	@Override
	public int valueOutput() {
		return 4;
	}

	@Override
	public int valuePwmOutput() {
		return 3;
	}

	@Override
	public int valueInput() {
		return 2;
	}

	@Override
	public int valueHigh() {
		return 1;
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
	public void pinMode(final int i, final int type) {

	}

	@Override
	public void digitalWrite(final int pin, final int val) {}

	@Override
	public void digitalWrite(final int pin, final boolean val) {}

	@Override
	public void pwmWrite(final int pin, final int val) {}

	@Override
	public void delayMicroseconds(final int t) {}

	@Override
	public int digitalRead(final int pin) {
		return 0;
	}

	@Override
	public Object getBoardType() {
		return new Object();
	}

}
