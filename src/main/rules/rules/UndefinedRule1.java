package rules;
/*
SETTINGS: (please don't move this part)
 PATH=UndefinedRule1
*/

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.functions.Undefined;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Undefined rule
 * 0^0=undefined
 * 
 * @author Andrea Cavalli
 *
 */
public class UndefinedRule1 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "UndefinedRule1";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.EXISTENCE;
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
			MathContext root = f.getMathContext();
			FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1().equals(new Number(root, 0)) && fnc.getParameter2().equals(new Number(root, 0))) {
				isExecutable = true;
			}
		}

		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(new Undefined(root));
			return result;
		} else {
			return null;
		}
	}
}
