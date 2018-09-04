package it.cavallium.warppi.teavm;

import it.cavallium.warppi.ClassUtils;

public class TeaVMGpio implements it.cavallium.warppi.deps.Platform.Gpio {

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
	public void wiringPiSetupPhys() {
	}

	@Override
	public void pinMode(int i, int type) {
	}

	@Override
	public void digitalWrite(int pin, int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void digitalWrite(int pin, boolean val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void pwmWrite(int pin, int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}
		
	@Override
	public void delayMicroseconds(int t) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}
		
	@Override
	public int digitalRead(int pin) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public Object getBoardType() {
		return new Object();
	}

}
