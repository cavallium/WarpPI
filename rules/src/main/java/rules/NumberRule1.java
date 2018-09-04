package rules;
/*
SETTINGS: (please don't move this part)
 PATH=NumberRule1
*/

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * a * 0 = 0
 * 
 * @author Andrea Cavalli
 *
 */
public class NumberRule1 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "NumberRule1";
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
		if (f instanceof Multiplication) {
			MathContext root = f.getMathContext();
			FunctionOperator mult = (FunctionOperator) f;
			if (mult.getParameter1() instanceof Number) {
				Function numb = mult.getParameter1();
				if (numb.equals(new Number(root, 0))) {
					isExecutable = true;
				}
			}
			if (mult.getParameter2() instanceof Number) {
				Function numb = mult.getParameter2();
				if (numb.equals(new Number(root, 0))) {
					isExecutable = true;
				}
			}
		}

		if (isExecutable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(new Number(f.getMathContext(), 0));
			return result;
		} else {
			return null;
		}
	}
}
