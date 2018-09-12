package it.cavallium.warppi.math.parser.features.interfaces;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;

public abstract interface Feature {

	public Function toFunction(MathContext context) throws Error;

}
