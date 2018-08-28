package org.warp.picalculator.deps;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.ClassUtils.Var;

public class DGpio {

	public static final int OUTPUT = com.pi4j.wiringpi.Gpio.OUTPUT;
	public static final int PWM_OUTPUT = com.pi4j.wiringpi.Gpio.PWM_OUTPUT;
	public static final int INPUT = com.pi4j.wiringpi.Gpio.INPUT;
	public static final int HIGH = com.pi4j.wiringpi.Gpio.HIGH;
	public static final int LOW = com.pi4j.wiringpi.Gpio.LOW;
	public static final Object UnknownBoardType = com.pi4j.system.SystemInfo.BoardType.UNKNOWN;

	public static void wiringPiSetupPhys() {
		com.pi4j.wiringpi.Gpio.wiringPiSetupPhys();
	}

	public static void pinMode(int i, int type) {
		com.pi4j.wiringpi.Gpio.pinMode(i, type);
	}

	public static void digitalWrite(int pin, int val) {
		com.pi4j.wiringpi.Gpio.digitalWrite(pin, val);
	}

	public static void digitalWrite(int pin, boolean val) {
		com.pi4j.wiringpi.Gpio.digitalWrite(pin, val);
	}

	public static void pwmWrite(int pin, int val) {
		com.pi4j.wiringpi.Gpio.pwmWrite(pin, val);
	}

	public static void delayMicroseconds(int t) {
		com.pi4j.wiringpi.Gpio.delayMicroseconds(t);
	}

	public static int digitalRead(int pin) {
		return com.pi4j.wiringpi.Gpio.digitalRead(pin);
	}

	public static Object getBoardType() {
		return ClassUtils.invokeStaticMethod("com.pi4j.system.SystemInfo.getBoardType");
	}

}
