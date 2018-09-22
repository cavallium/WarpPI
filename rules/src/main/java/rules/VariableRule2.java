package rules;
/*
SETTINGS: (please don't move this part)
 PATH=VariableRule2
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Variable rule
 * ax+x=(a+1)*x (a,b NUMBER; x VARIABLES)
 *
 * @author Andrea Cavalli
 *
 */
public class VariableRule2 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "VariableRule2";
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
		if (f instanceof Sum || f instanceof Subtraction) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Multiplication) {
				final FunctionOperator m1 = (FunctionOperator) fnc.getParameter1();
				if (m1.getParameter2().equals(fnc.getParameter2()))
					isExecutable = true;
			}
		}

		if (isExecutable) {
			final FunctionOperator fnc = (FunctionOperator) f;
			final MathContext root = fnc.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator m1 = (FunctionOperator) fnc.getParameter1();
			final Function a = m1.getParameter1();
			final Function x = fnc.getParameter2();

			Function rets;
			if (fnc instanceof Sum)
				rets = new Sum(root, a, new Number(root, 1));
			else
				rets = new Subtraction(root, a, new Number(root, 1));
			final Function retm = new Multiplication(root, rets, x);
			result.add(retm);
			return result;
		} else
			return null;
	}
}
