package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.util.Error;

public class FeatureParenthesis extends FeatureSingleImpl {

	public FeatureParenthesis(final Object child) {
		super(child);
	}

	@Override
	public Function toFunction(final MathContext context) throws Error {
		return new Expression(context, getFunction1());
	}

}
