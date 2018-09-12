package rules;
/*
SETTINGS: (please don't move this part)
 PATH=NumberRule3
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
 * a - a = 0
 * -a + a = 0
 * a ± a = {0, 2a}
 * 
 * @author Andrea Cavalli
 *
 */
public class NumberRule3 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "NumberRule3";
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
	public ObjectArrayList<Function> execute(Function f) {
		boolean isExecutable = false;
		if (f instanceof Subtraction) {
			FunctionOperator sub = (FunctionOperator) f;
			if (sub.getParameter1().equals(sub.getParameter2())) {
				isExecutable = true;
			}
		} else if (f instanceof Sum) {
			FunctionOperator sub = (FunctionOperator) f;
			if (sub.getParameter1() instanceof Multiplication) {
				if (((FunctionOperator) sub.getParameter1()).getParameter1() instanceof Number && ((FunctionOperator) sub.getParameter1()).getParameter1().equals(new Number(f.getMathContext(), -1))) {
					Function neg = ((FunctionOperator) sub.getParameter1()).getParameter2();
					if (neg.equals(sub.getParameter2())) {
						isExecutable = true;
					}
				}
			}
		} else if (f instanceof SumSubtraction) {
			FunctionOperator sub = (FunctionOperator) f;
			if (sub.getParameter1().equals(sub.getParameter2())) {
				isExecutable = true;
			}
		}

		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			if (f instanceof SumSubtraction) {
				Function mul = new Multiplication(root, new Number(root, 2), ((FunctionOperator) f).getParameter1());
				result.add(mul);
			}
			result.add(new Number(root, 0));
			return result;
		} else {
			return null;
		}
	}
}
