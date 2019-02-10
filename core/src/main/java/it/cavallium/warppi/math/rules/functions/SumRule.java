package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.SumRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
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
		}
		return null;
	}
}
