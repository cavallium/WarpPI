package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule4
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * (a*b)^n=a^n*b^n
 *
 * @author Andrea Cavalli
 *
 */
public class ExponentRule4 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule4";
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
			if (fnc.getParameter1() instanceof Multiplication && fnc.getParameter2() instanceof Number)
				isExecutable = true;
		}

		if (isExecutable) {
			final MathContext root = f.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			final FunctionOperator mult = (FunctionOperator) fnc.getParameter1();
			final Function a = mult.getParameter1();
			final Function b = mult.getParameter2();
			final Function n = fnc.getParameter2();
			final Function p1 = new Power(root, a, n);
			final Function p2 = new Power(root, b, n);
			final Function retMult = new Multiplication(root, p1, p2);
			result.add(retMult);
			return result;
		} else
			return null;
	}
}
