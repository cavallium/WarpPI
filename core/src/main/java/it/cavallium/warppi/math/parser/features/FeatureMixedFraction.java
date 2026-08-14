package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;

/** A whole part followed by a proper fraction, as entered by the shifted fraction key. */
public final class FeatureMixedFraction implements Feature {
	private final Function whole;
	private final Function numerator;
	private final Function denominator;

	public FeatureMixedFraction(final Function whole, final Function numerator,
			final Function denominator) {
		this.whole = whole;
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public Function toFunction(final MathContext context) throws Error {
		if (!(whole instanceof Number wholeNumber)
				|| !(numerator instanceof Number numeratorNumber)
				|| !(denominator instanceof Number denominatorNumber)
				|| !wholeNumber.isInteger() || !numeratorNumber.isInteger()
				|| !denominatorNumber.isInteger())
			throw new Error(Errors.SYNTAX_ERROR,
					"Mixed-fraction fields must be integer numbers.");
		if (numeratorNumber.getTerm().signum() < 0
				|| denominatorNumber.getTerm().signum() <= 0)
			throw new Error(Errors.SYNTAX_ERROR,
					"A mixed fraction needs a non-negative numerator and positive denominator.");
		final Division fraction = new Division(context, numerator, denominator);
		if (wholeNumber.getTerm().signum() < 0)
			return new Subtraction(context, whole, fraction);
		return new Sum(context, whole, fraction);
	}
}
