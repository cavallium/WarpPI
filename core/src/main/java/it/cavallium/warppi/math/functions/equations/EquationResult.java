package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.math.functions.Number;

public class EquationResult {
	public boolean isAnEquation = false;
	public Number LR;

	public EquationResult(Number LR, boolean isAnEquation) {
		this.LR = LR;
		this.isAnEquation = isAnEquation;
	}
}
