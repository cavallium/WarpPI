package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule17
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.functions.Root;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * a√x=x^1/a
 *
 * @author Andrea Cavalli
 *
 */
public class ExponentRule17 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule17";
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
	public ObjectArrayList<Function> execute(final Function f) { // FIXME incorrect rule
		boolean isExecutable = false;
		if (f instanceof Root) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1().equals(fnc.getParameter2()))
				isExecutable = true; // root(a, a)
		}

		if (isExecutable) {
			final MathContext root = f.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			final Function a = fnc.getParameter1();
			final Function two = new Number(root, 2);
			final Function p = new Power(root, a, two);
			result.add(p);
			return result; // root(a, a) -> a^2
		} else
			return null;
	}
}
