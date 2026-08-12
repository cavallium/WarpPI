package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.util.Error;

public class FeatureEquation extends FeatureDoubleImpl {

	public FeatureEquation(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Equation toFunction(final MathContext context) throws Error {
		return new Equation(context, getFunction1(), getFunction2());
	}

}
