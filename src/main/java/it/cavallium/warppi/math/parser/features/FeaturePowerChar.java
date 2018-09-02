package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;

public class FeaturePowerChar extends FeatureSingleImpl {

	public FeaturePowerChar(Object child) {
		super(child);
	}

	@Override
	public Function toFunction(MathContext context) throws Error {
		return null;
	}

}
