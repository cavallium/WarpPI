package it.cavallium.warppi.math.rules.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.PrimeFactorization;
import it.cavallium.warppi.math.functions.ScientificFunction;
import it.cavallium.warppi.math.functions.Sexagesimal;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ScientificFunctionRule implements Rule {
	@Override public String getRuleName() { return "ScientificFunction"; }
	@Override public RuleType getRuleType() { return RuleType.CALCULATION; }

	@Override
	public ObjectArrayList<Function> execute(final Function function) throws Error {
		if (!(function instanceof ScientificFunction scientific)
				|| !(scientific.getParameter() instanceof Number number))
			return null;

		final MathContext context = function.getMathContext();
		final Function result = switch (scientific.getKind()) {
			case ABSOLUTE -> new Number(context, number.getTerm().abs());
			case ROUND -> new Number(context,
					number.getTerm().setScale(Utils.displayScale, Utils.scaleMode2));
			case PERCENT -> new Division(context, number, new Number(context, 100));
			case FACTORIAL -> factorial(context, number);
			case FACTORIZE -> factorize(context, number);
			case DEGREE_MINUTE_SECOND -> sexagesimal(context, number);
		};
		return result == null ? null : new ObjectArrayList<>(new Function[] { result });
	}

	private static Function factorial(final MathContext context, final Number number) throws Error {
		if (!number.isInteger() || number.getTerm().signum() < 0)
			throw new Error(Errors.NEGATIVE_PARAMETER, "Factorial requires a non-negative integer.");
		final BigInteger value = number.getTerm().toBigIntegerExact();
		if (value.compareTo(BigInteger.valueOf(10000)) > 0)
			throw new Error(Errors.NUMBER_TOO_LARGE, "Factorial input exceeds 10000.");
		BigInteger factorial = BigInteger.ONE;
		for (BigInteger factor = BigInteger.TWO; factor.compareTo(value) <= 0;
				factor = factor.add(BigInteger.ONE))
			factorial = factorial.multiply(factor);
		return new Number(context, factorial);
	}

	private static Function factorize(final MathContext context, final Number number) {
		if (!number.isInteger() || number.getTerm().abs().compareTo(BigDecimal.ONE) <= 0)
			return null;
		final LinkedList<BigInteger> factors = number.getFactors();
		if (factors.isEmpty())
			return null;
		final Function[] factorFunctions = factors.stream()
				.map(factor -> (Function) new Number(context, factor)).toArray(Function[]::new);
		return new PrimeFactorization(context, factorFunctions);
	}

	private static Function sexagesimal(final MathContext context, final Number number) {
		final BigDecimal absolute = number.getTerm().abs();
		final BigInteger degrees = absolute.toBigInteger();
		final BigDecimal minutesValue = absolute.subtract(new BigDecimal(degrees))
				.multiply(BigDecimal.valueOf(60));
		final BigInteger minutes = minutesValue.toBigInteger();
		final BigDecimal seconds = minutesValue.subtract(new BigDecimal(minutes))
				.multiply(BigDecimal.valueOf(60)).setScale(Utils.displayScale, Utils.scaleMode2);
		return new Sexagesimal(context, number.getTerm().signum() < 0,
				degrees, minutes, seconds);
	}
}
