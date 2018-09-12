package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.RootSquare;
import it.cavallium.warppi.util.Error;

public class FeatureSquareRoot extends FeatureSingleImpl {

	public FeatureSquareRoot(Object child) {
		super(child);
	}

	@Override
	public RootSquare toFunction(MathContext context) throws Error {
		return new RootSquare(context, getFunction1());
	}

}
