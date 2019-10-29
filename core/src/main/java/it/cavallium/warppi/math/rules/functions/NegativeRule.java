package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.NegativeRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Negative
 * -a = b
 *
 * @author Andrea Cavalli
 *
 */
public class NegativeRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Negative";
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
	public ObjectArrayList<Function> execute(final Function f) throws Error {
		if (f instanceof Negative) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Function variable = ((Negative) f).getParameter();
			final MathContext mathContext = f.getMathContext();
			if (variable instanceof Number) {
				//-a = a*-1 = b
				try {
					result.add(((Number) variable).multiply(new Number(mathContext, -1)));
				} catch (final Exception ex) {
					if (ex instanceof NullPointerException)
						throw new Error(Errors.ERROR);
					else if (ex instanceof NumberFormatException)
						throw new Error(Errors.SYNTAX_ERROR);
					else if (ex instanceof ArithmeticException)
						throw new Error(Errors.NUMBER_TOO_SMALL);
				}
				return result;
			}
		}
		return null;
	}
}
