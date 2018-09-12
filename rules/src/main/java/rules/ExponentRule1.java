package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExponentRule1
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Exponent rule
 * 1^a=1
 * 
 * @author Andrea Cavalli
 *
 */
public class ExponentRule1 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExponentRule1";
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
		MathContext root = f.getMathContext();
		if (f instanceof Power) {
			if (((Power)f).getParameter1().equals(new Number(root, 1))) {
				isExecutable = true;
			}
		}
	
		if (isExecutable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(new Number(root, 1));
			return result;
		} else {
			return null;
		}
	}

}
