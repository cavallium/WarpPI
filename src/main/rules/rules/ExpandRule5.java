package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExpandRule5
*/

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Expand rule
 * -(-a) = a
 * 
 * @author Andrea Cavalli
 *
 */
public class ExpandRule5 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExpandRule5";
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
		if (f instanceof Negative) {
			isExecutable = ((FunctionSingle)f).getParameter() instanceof Negative;
		} else if (f instanceof Multiplication) {
			if (((FunctionOperator)f).getParameter1().equals(new Number(f.getMathContext(), -1)) && ((FunctionOperator)f).getParameter2() instanceof Multiplication) {
				isExecutable = ((FunctionOperator)((FunctionOperator)f).getParameter2()).getParameter1().equals(((FunctionOperator)f).getParameter1());
			}
		}
	
		if (isExecutable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
	
			if (f instanceof Negative) {
				Negative fnc = (Negative) f;
				result.add(((FunctionSingle)((FunctionSingle)fnc.getParameter()).getParameter()).getParameter());
			} else if (f instanceof Multiplication) {
				FunctionOperator fnc = (FunctionOperator) f;
				result.add(((FunctionOperator)fnc.getParameter2()).getParameter2());
			}
			return result;
		} else {
			return null;
		}
	}

}
