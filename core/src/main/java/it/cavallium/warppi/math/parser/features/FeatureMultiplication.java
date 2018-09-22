package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;

public class FeatureMultiplication extends FeatureDoubleImpl {

	public FeatureMultiplication(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Multiplication toFunction(final MathContext context) {
		return new Multiplication(context, getFunction1(), getFunction2());
	}

}
