package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.MultiplicationRule
*/

import it.cavallium.warppi.ScriptUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.RulesManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Multiplication
 * a*b = c
 * 
 * @author Andrea Cavalli
 *
 */
public class MultiplicationRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Multiplication";
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
		if (f instanceof Multiplication) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			Function variable1 = ((Multiplication) f).getParameter1();
			Function variable2 = ((Multiplication) f).getParameter2();
			MathContext mathContext = f.getMathContext();
			if (variable1 instanceof Number && variable2 instanceof Number) {
				//multiply a by b (a*b = c)
				result.add(((Number) variable1).multiply((Number) variable2));
				return result;
			}
		}
		return null;
	}
}
