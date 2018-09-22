package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.math.functions.Number;

public class EquationResult {
	public boolean isAnEquation = false;
	public Number LR;

	public EquationResult(final Number LR, final boolean isAnEquation) {
		this.LR = LR;
		this.isAnEquation = isAnEquation;
	}
}
