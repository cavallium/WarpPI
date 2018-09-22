package rules;
/*
SETTINGS: (please don't move this part)
 PATH=NumberRule5
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * a + 0 = a
 * 0 + a = a
 * a - 0 = a
 * 0 - a = -a
 * a ± 0 = a
 *
 * @author Andrea Cavalli
 *
 */
public class NumberRule5 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "NumberRule5";
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
		if (f instanceof Sum || f instanceof Subtraction || f instanceof SumSubtraction) {
			final MathContext root = f.getMathContext();
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1().equals(new Number(root, 0)) || fnc.getParameter2().equals(new Number(root, 0)))
				if (!(fnc.getParameter1().equals(new Number(root, 0)) && f instanceof SumSubtraction))
					isExecutable = true;
		}

		if (isExecutable) {
			final MathContext root = f.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			Function a = fnc.getParameter1();
			if (a.equals(new Number(root, 0)))
				if (f instanceof Subtraction)
					a = new Multiplication(root, new Number(root, -1), fnc.getParameter2());
				else
					a = fnc.getParameter2();
			result.add(a);
			return result;
		} else
			return null;
	}
}
