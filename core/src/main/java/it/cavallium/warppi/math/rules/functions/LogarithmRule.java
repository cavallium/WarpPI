package it.cavallium.warppi.math.rules.functions;

import java.math.BigDecimal;

import org.nevec.rjm.BigDecimalMath;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Logarithm;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class LogarithmRule implements Rule {
	@Override public String getRuleName() { return "Logarithm"; }
	@Override public RuleType getRuleType() { return RuleType.CALCULATION; }

	@Override
	public ObjectArrayList<Function> execute(final Function function) throws Error {
		if (!(function instanceof Logarithm logarithm)
				|| !(logarithm.getParameter1() instanceof Number base)
				|| !(logarithm.getParameter2() instanceof Number value)
				|| function.getMathContext().exactMode)
			return null;
		if (base.getTerm().signum() <= 0 || base.getTerm().compareTo(BigDecimal.ONE) == 0
				|| value.getTerm().signum() <= 0)
			throw new Error(Errors.NEGATIVE_PARAMETER, "The logarithm base or argument is outside its domain.");
		final BigDecimal result = BigDecimalMath.log(value.getTerm())
				.divide(BigDecimalMath.log(base.getTerm()), new java.math.MathContext(24));
		return new ObjectArrayList<>(new Function[] { new Number(function.getMathContext(), result) });
	}
}
