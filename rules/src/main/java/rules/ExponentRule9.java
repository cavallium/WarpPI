package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule9
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * (a ^ b) ^ c = a ^ (b * c)
 * 
 * @author Andrea Cavalli
 *
 */
public class ExponentRule9 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule9";
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
		if (f instanceof Power) {
			FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Power) {
				isExecutable = true;
			}
		}
	
		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator powC = (FunctionOperator) f;
			FunctionOperator powB = (FunctionOperator) powC.getParameter1();
			Function p = new Power(root, powB.getParameter1(), new Multiplication(root, powB.getParameter2(), powC.getParameter2()));
			result.add(p);
			return result;
		} else {
			return null;
		}
	}
}
