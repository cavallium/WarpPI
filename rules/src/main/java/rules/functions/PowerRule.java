package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.PowerRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Power
 * a^b = c
 * 
 * @author Andrea Cavalli
 *
 */
public class PowerRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Power";
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
	public ObjectArrayList<Function> execute(Function f) throws it.cavallium.warppi.util.Error, InterruptedException {
		if (f instanceof Power) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			Function variable1 = ((FunctionOperator) f).getParameter1();
			Function variable2 = ((FunctionOperator) f).getParameter2();
			MathContext mathContext = f.getMathContext();
			if (variable1 instanceof Number && variable2 instanceof Number) {
				//a^b = c
				Number out = ((Number) variable1).pow((Number) variable2);
				if (mathContext.exactMode && !out.isInteger()) {
					return null;
				}
				result.add(out);
				return result;
			}
		}
		return null;
	}
}
