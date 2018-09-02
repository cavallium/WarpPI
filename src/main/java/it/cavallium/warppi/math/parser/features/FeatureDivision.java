package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;

public class FeatureDivision extends FeatureDoubleImpl {

	public FeatureDivision(Object child1, Object child2) {
		super(child1, child2);
	}

	@Override
	public Division toFunction(MathContext context) {
		return new Division(context, getFunction1(), getFunction2());
	}

}
