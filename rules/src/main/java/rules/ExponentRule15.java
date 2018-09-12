package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule15
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
 * a*a=a^2
 * 
 * @author Andrea Cavalli
 *
 */
public class ExponentRule15 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule15";
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
		if (f instanceof Multiplication) {
			FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1().equals(fnc.getParameter2())) {
				isExecutable = true;
			}
		}
	
		if (isExecutable) {
		MathContext root = f.getMathContext();
		ObjectArrayList<Function> result = new ObjectArrayList<>();
		FunctionOperator fnc = (FunctionOperator) f;
		Function a = fnc.getParameter1();
		Function two = new Number(root, 2);
		Function p = new Power(root, a, two);
		result.add(p);
		return result;
		} else {
			return null;
		}
	}
}
