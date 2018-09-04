package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Sine;

public class FeatureSine extends FeatureSingleImpl {

	public FeatureSine(Object child) {
		super(child);
	}

	@Override
	public Function toFunction(MathContext context) throws Error {
		return new Sine(context, getFunction1());
	}

}
