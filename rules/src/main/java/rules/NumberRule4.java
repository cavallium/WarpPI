package rules;
/*
SETTINGS: (please don't move this part)
 PATH=NumberRule4
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * a ± b = {a+b, a-b}
 * 
 * @author Andrea Cavalli
 *
 */
public class NumberRule4 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "NumberRule4";
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
	public ObjectArrayList<Function> execute(Function f) {
		boolean isExecutable = false;
		if (f instanceof SumSubtraction) {
			isExecutable = true;
		}

		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator ss = (FunctionOperator) f;
			result.add(new Sum(root, ss.getParameter1(), ss.getParameter2()));
			result.add(new Subtraction(root, ss.getParameter1(), ss.getParameter2()));
			return result;
		} else {
			return null;
		}
	}
}
