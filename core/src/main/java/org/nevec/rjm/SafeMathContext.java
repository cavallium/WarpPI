package org.nevec.rjm;

import java.math.MathContext;
import java.math.RoundingMode;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;

public final class SafeMathContext {

	public static MathContext newMathContext(int precision) {
		if (precision <= 0) {
			Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Warning! MathContext precision is <= 0 (" + precision + ")");
			precision = 1;
		}
		return new MathContext(precision);
	}

	public static MathContext newMathContext(int precision, final RoundingMode roundingMode) {
		if (precision <= 0) {
			Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Warning! MathContext precision is <= 0 (" + precision + ")");
			precision = 1;
		}
		return new MathContext(precision, roundingMode);
	}

}
