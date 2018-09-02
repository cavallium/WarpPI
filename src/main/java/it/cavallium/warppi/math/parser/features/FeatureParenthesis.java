package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;

public class FeatureParenthesis extends FeatureSingleImpl {

	public FeatureParenthesis(Object child) {
		super(child);
	}

	@Override
	public Function toFunction(MathContext context) throws Error {
		return new Expression(context, getFunction1());
	}

}
