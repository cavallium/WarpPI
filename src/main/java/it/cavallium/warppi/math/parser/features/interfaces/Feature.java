package it.cavallium.warppi.math.parser.features.interfaces;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;

public abstract interface Feature {

	public Function toFunction(MathContext context) throws Error;

}
