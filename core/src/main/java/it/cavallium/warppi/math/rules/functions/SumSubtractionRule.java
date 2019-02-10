package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.SumSubtractionRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * SumSumbraction
 * a±b = c, d
 *
 * @author Andrea Cavalli
 *
 */
public class SumSubtractionRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "SumSubtraction";
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
		if (f instanceof SumSubtraction) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Function variable1 = ((FunctionOperator) f).getParameter1();
			final Function variable2 = ((FunctionOperator) f).getParameter2();
			final MathContext mathContext = f.getMathContext();
			if (variable1 instanceof Number && variable2 instanceof Number) {
				//a±b = c, d
				result.add(((Number) variable1).add((Number) variable2));
				result.add(((Number) variable1).add(((Number) variable2).multiply(new Number(mathContext, -1))));
				return result;
			}
		}
		return null;
	}
}
