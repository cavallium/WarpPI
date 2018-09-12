package it.cavallium.warppi.hardware;

import java.io.IOException;

public class HardwareGpio implements it.cavallium.warppi.Platform.Gpio {

	@Override
	public int valueOutput() {
		return com.pi4j.wiringpi.Gpio.OUTPUT;
	}

	@Override
	public int valuePwmOutput() {
		return com.pi4j.wiringpi.Gpio.PWM_OUTPUT;
	}

	@Override
	public int valueInput() {
		return com.pi4j.wiringpi.Gpio.INPUT;
	}

	@Override
	public int valueHigh() {
		return com.pi4j.wiringpi.Gpio.HIGH;
	}

	@Override
	public int valueLow() {
		return com.pi4j.wiringpi.Gpio.LOW;
	}

	@Override
	public Object valueUnknownBoardType() {
		return com.pi4j.system.SystemInfo.BoardType.UNKNOWN;
	}

	@Override
	public void wiringPiSetupPhys() {
		com.pi4j.wiringpi.Gpio.wiringPiSetupPhys();
	}

	@Override
	public void pinMode(int i, int type) {
		com.pi4j.wiringpi.Gpio.pinMode(i, type);
		
	}

	@Override
	public void digitalWrite(int pin, int val) {
		com.pi4j.wiringpi.Gpio.digitalWrite(pin, val);
	}

	@Override
	public void digitalWrite(int pin, boolean val) {
		com.pi4j.wiringpi.Gpio.digitalWrite(pin, val);
	}

	@Override
	public void pwmWrite(int pin, int val) {
		com.pi4j.wiringpi.Gpio.pwmWrite(pin, val);
	}

	@Override
	public void delayMicroseconds(int t) {
		com.pi4j.wiringpi.Gpio.delayMicroseconds(t);
	}

	@Override
	public int digitalRead(int pin) {
		return com.pi4j.wiringpi.Gpio.digitalRead(pin);
	}

	@Override
	public Object getBoardType() {
		try {
			return com.pi4j.system.SystemInfo.getBoardType();
		} catch (UnsupportedOperationException | IOException | InterruptedException e) {
			e.printStackTrace();
			return new Object();
		}
	}

}
