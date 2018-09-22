package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule4
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Fractions rule
 * (a / b) ^ -1 = b / a
 *
 * @author Andrea Cavalli
 *
 */
public class FractionsRule4 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule4";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.EXPANSION;
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
			if (fnc.getParameter1() instanceof Division && fnc.getParameter2() instanceof Number) {
				final Function n2 = fnc.getParameter2();
				if (n2.equals(new Number(f.getMathContext(), -1)))
					isExecutable = true;
			}
		}

		if (isExecutable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			final Function a = ((FunctionOperator) fnc.getParameter1()).getParameter1();
			final Function b = ((FunctionOperator) fnc.getParameter1()).getParameter2();
			final Function res = new Division(f.getMathContext(), b, a);
			result.add(res);
			return result;
		} else
			return null;
	}
}
