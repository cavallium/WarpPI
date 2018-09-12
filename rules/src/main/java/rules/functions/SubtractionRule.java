package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.SubtractionRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Subtraction
 * a-b = c
 * 
 * @author Andrea Cavalli
 *
 */
public class SubtractionRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Subtraction";
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
		if (f instanceof Subtraction) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			Function variable1 = ((FunctionOperator) f).getParameter1();
			Function variable2 = ((FunctionOperator) f).getParameter2();
			MathContext mathContext = f.getMathContext();
			if (variable1 instanceof Number && variable2 instanceof Number) {
				//a-b = a+(b*-1) = c
				result.add(((Number) variable1).add(((Number) variable2).multiply(new Number(mathContext, -1))));
				return result;
			}
		}
		return null;
	}
}
