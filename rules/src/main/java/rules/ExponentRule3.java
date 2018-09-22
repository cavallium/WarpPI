package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule3
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * a^0=1
 *
 * @author Andrea Cavalli
 *
 */
public class ExponentRule3 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule3";
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
		boolean isExecutable = false;
		if (f instanceof Power) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter2().equals(new Number(f.getMathContext(), 0)))
				isExecutable = true;
		}

		if (isExecutable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(new Number(f.getMathContext(), 1));
			return result;
		} else
			return null;
	}
}
