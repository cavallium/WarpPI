package it.cavallium.warppi.math.rules.functions;

import java.math.BigInteger;
import java.util.Random;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.RandomInteger;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class RandomIntegerRule implements Rule {
	private static final Random RANDOM = new Random();

	@Override public String getRuleName() { return "RandomInteger"; }
	@Override public RuleType getRuleType() { return RuleType.CALCULATION; }

	@Override
	public ObjectArrayList<Function> execute(final Function function) throws Error {
		if (!(function instanceof RandomInteger randomInteger)
				|| !(randomInteger.getParameter1() instanceof Number minimum)
				|| !(randomInteger.getParameter2() instanceof Number maximum))
			return null;
		if (!minimum.isInteger() || !maximum.isInteger())
			throw new Error(Errors.CONVERSION_ERROR, "RanInt bounds must be integers.");

		final BigInteger lower = minimum.getTerm().toBigIntegerExact();
		final BigInteger upper = maximum.getTerm().toBigIntegerExact();
		if (lower.compareTo(upper) > 0)
			throw new Error(Errors.NEGATIVE_PARAMETER, "RanInt minimum exceeds its maximum.");
		final BigInteger range = upper.subtract(lower).add(BigInteger.ONE);
		if (range.bitLength() > 31)
			throw new Error(Errors.NUMBER_TOO_LARGE, "RanInt range is too large.");
		final BigInteger value = lower.add(BigInteger.valueOf(RANDOM.nextInt(range.intValueExact())));
		return new ObjectArrayList<>(new Function[] { new Number(function.getMathContext(), value) });
	}
}
