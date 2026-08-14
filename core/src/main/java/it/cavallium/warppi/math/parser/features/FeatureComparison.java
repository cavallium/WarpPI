package it.cavallium.warppi.math.parser.features;

import java.util.Objects;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Comparison;
import it.cavallium.warppi.math.functions.Comparison.Operator;
import it.cavallium.warppi.util.Error;

public final class FeatureComparison extends FeatureDoubleImpl {
	private final Operator operator;

	public FeatureComparison(final Operator operator, final Object left, final Object right) {
		super(left, right);
		this.operator = Objects.requireNonNull(operator, "operator");
	}

	@Override
	public Comparison toFunction(final MathContext context) throws Error {
		return new Comparison(context, operator, getFunction1(), getFunction2());
	}
}
