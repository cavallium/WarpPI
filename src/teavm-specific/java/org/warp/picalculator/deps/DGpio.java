package org.warp.picalculator.deps;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.ClassUtils.Var;

public class DGpio {

	public static final int OUTPUT = 0;
	public static final int PWM_OUTPUT =  1;
	public static final int INPUT = 2;
	public static final int HIGH = 3;
	public static final int LOW = 4;
	public static final Object UnknownBoardType = new Object();

	public static void wiringPiSetupPhys() {
	}

	public static void pinMode(int i, int type) {
	}

	public static void digitalWrite(int pin, int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	public static void digitalWrite(int pin, boolean val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	public static void pwmWrite(int pin, int val) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	public static void delayMicroseconds(int t) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	public static int digitalRead(int pin) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	public static Object getBoardType() {
		return new Object();
	}

}
