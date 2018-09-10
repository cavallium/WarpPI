package it.cavallium.warppi.desktop;

import it.cavallium.warppi.ClassUtils;

public class DesktopGpio implements it.cavallium.warppi.deps.Platform.Gpio {

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
	public void wiringPiSetupPhys() {
	}

	@Override
	public void pinMode(int i, int type) {
		
	}

	@Override
	public void digitalWrite(int pin, int val) {
	}

	@Override
	public void digitalWrite(int pin, boolean val) {
	}

	@Override
	public void pwmWrite(int pin, int val) {
	}

	@Override
	public void delayMicroseconds(int t) {
	}

	@Override
	public int digitalRead(int pin) {
		return 0;
	}

	@Override
	public Object getBoardType() {
		return new Object();
	}

}
