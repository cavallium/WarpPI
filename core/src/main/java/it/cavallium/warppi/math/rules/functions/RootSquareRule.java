package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.RootSquareRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Root;
import it.cavallium.warppi.math.functions.RootSquare;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Root Square
 * √b = c
 *
 * @author Andrea Cavalli
 *
 */
public class RootSquareRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "RootSquare";
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
	public ObjectArrayList<Function> execute(final Function f) throws Error, InterruptedException {
		if (f instanceof RootSquare) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final MathContext mathContext = f.getMathContext();
			final Function variable = ((RootSquare) f).getParameter();
			final var degree = ((RootSquare) f).getDegree();
			boolean isSolvable = false, canBePorted = false;
			if (variable instanceof Number) {
				/*if (mathContext.exactMode) {
					result.add(((Number) variable).pow(new Number(mathContext, BigDecimal.ONE).divide(degree)));
					return result;
				}*/
				isSolvable = isSolvable | !mathContext.exactMode;
				if (!isSolvable)
					try {
						final Number resultVar = ((Number) variable).pow(new Number(mathContext, BigDecimal.ONE).divide(degree));
						final Function originalVariable = resultVar.pow(new Number(mathContext, 2));
						if (originalVariable.equals(((RootSquare) f).getParameter()))
							isSolvable = true;
					} catch (final Exception ex) {
						throw (Error) new Error(Errors.ERROR, ex.getMessage()).initCause(ex);
					}
			}

			if (isSolvable) {
				result.add(((Number) variable).pow(new Number(mathContext, BigInteger.ONE).divide(degree)));
				return result;
			}
		}
		return null;
	}
}
