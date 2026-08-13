package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.SumRule
*/

import java.math.BigInteger;

import org.nevec.rjm.Rational;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Sum
 * a+b = c
 *
 * @author Andrea Cavalli
 *
 */
public class SumRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Sum";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.CALCULATION;
	}

	/* Rule function
	   Returns:
	     - null if it's not executable on the function "f"
	     - An ObjectArrayList<Function> if it did something
	*/
	@Override
	public ObjectArrayList<Function> execute(final Function f) {
		if (f instanceof Sum) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Function variable1 = ((FunctionOperator) f).getParameter1();
			final Function variable2 = ((FunctionOperator) f).getParameter2();
			if (variable1 instanceof Number && variable2 instanceof Number) {
				//a+b = c
				result.add(((Number) variable1).add((Number) variable2));
				return result;
			}
			if (f.getMathContext().exactMode) {
				final Function exactSum = addExactRationals(f.getMathContext(), variable1, variable2);
				if (exactSum != null) {
					result.add(exactSum);
					return result;
				}
			}
		}
		return null;
	}

	private static Function addExactRationals(final MathContext mathContext,
			final Function left, final Function right) {
		final Rational leftValue = toRational(left);
		final Rational rightValue = toRational(right);
		if (leftValue == null || rightValue == null) {
			return null;
		}

		final Rational sum = leftValue.add(rightValue);
		if (sum.denom().equals(BigInteger.ONE)) {
			return new Number(mathContext, sum.numer());
		}
		return new Division(mathContext, new Number(mathContext, sum.numer()),
				new Number(mathContext, sum.denom()));
	}

	private static Rational toRational(final Function function) {
		if (function instanceof Number) {
			final Number number = (Number) function;
			return number.isInteger()
					? new Rational(number.getTerm().toBigIntegerExact()) : null;
		}
		if (!(function instanceof Division)) {
			return null;
		}

		final Division division = (Division) function;
		if (!(division.getParameter1() instanceof Number)
				|| !(division.getParameter2() instanceof Number)) {
			return null;
		}
		final Number numerator = (Number) division.getParameter1();
		final Number denominator = (Number) division.getParameter2();
		if (!numerator.isInteger() || !denominator.isInteger()
				|| denominator.getTerm().signum() == 0) {
			return null;
		}
		return new Rational(numerator.getTerm().toBigIntegerExact(),
				denominator.getTerm().toBigIntegerExact());
	}
}
