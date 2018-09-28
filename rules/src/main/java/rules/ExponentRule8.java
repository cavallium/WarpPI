package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule8
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * (a) ^ (b+c) = a ^ b * a ^ c
 *
 * @author Andrea Cavalli
 *
 */
public class ExponentRule8 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule8";
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
		if (f instanceof Power) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter2() instanceof Sum) {
				final Sum sum = (Sum) fnc.getParameter2();
				final MathContext root = f.getMathContext();
				final ObjectArrayList<Function> result = new ObjectArrayList<>();
				final Function a = fnc.getParameter1();
				final Function b = sum.getParameter1();
				final Function c = sum.getParameter2();
				result.add(new Multiplication(root, new Power(root, a, b), new Power(root, a, c)));
				return result;
			}
		}

		return null;
	}
}
