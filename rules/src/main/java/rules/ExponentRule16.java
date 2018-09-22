package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule16
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * (a ^ b) * (a ^ c) = a ^ (b + c)
 *
 * @author Andrea Cavalli
 *
 */
public class ExponentRule16 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule16";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.REDUCTION;
	}

	/* Rule function
	   Returns:
	     - null if it's not executable on the function "f"
	     - An ObjectArrayList<Function> if it did something
	*/

	@Override
	public ObjectArrayList<Function> execute(final Function f) {
		boolean isExecutable = false;
		if (f instanceof Multiplication) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Power && fnc.getParameter2() instanceof Power)
				isExecutable = ((FunctionOperator) fnc.getParameter1()).getParameter1().equals(((FunctionOperator) fnc.getParameter2()).getParameter1());
			else if (fnc.getParameter1() instanceof Power)
				isExecutable = ((FunctionOperator) fnc.getParameter1()).getParameter1().equals(fnc.getParameter2());
			else if (fnc.getParameter2() instanceof Power)
				isExecutable = ((FunctionOperator) fnc.getParameter2()).getParameter1().equals(fnc.getParameter1());
		}

		if (isExecutable) {
			final MathContext root = f.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Power && fnc.getParameter2() instanceof Power)
				result.add(new Power(root, ((FunctionOperator) fnc.getParameter1()).getParameter1(), new Sum(root, ((FunctionOperator) fnc.getParameter1()).getParameter2(), ((FunctionOperator) fnc.getParameter2()).getParameter2())));
			else if (fnc.getParameter1() instanceof Power)
				result.add(new Power(root, fnc.getParameter2(), new Sum(root, ((FunctionOperator) fnc.getParameter1()).getParameter2(), new Number(root, 1))));
			else if (fnc.getParameter2() instanceof Power)
				result.add(new Power(root, fnc.getParameter1(), new Sum(root, new Number(root, 1), ((FunctionOperator) fnc.getParameter2()).getParameter2())));
			return result;
		} else
			return null;
	}
}
