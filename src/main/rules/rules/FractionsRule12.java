package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule12
*/

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Fractions rule
 * (b / c) / a = b / (a * c)
 * 
 * @author Andrea Cavalli
 *
 */
public class FractionsRule12 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule12";
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
		if (f instanceof Division) {
			FunctionOperator fnc = (FunctionOperator) f;
			Function a;
			Function c;
			if (fnc.getParameter1() instanceof Division) {
				FunctionOperator div2 = (FunctionOperator) fnc.getParameter1();
				a = fnc.getParameter1();
				c = div2.getParameter2();
				isExecutable = true;
			}
		}
	
		if (isExecutable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator fnc = (FunctionOperator) f;
			Function a;
			Function b;
			Function c;
	
			FunctionOperator div2 = (FunctionOperator) fnc.getParameter1();
			a = fnc.getParameter2();
			b = div2.getParameter1();
			c = div2.getParameter2();
			result.add(new Division(fnc.getMathContext(), b, new Multiplication(fnc.getMathContext(), c, a)));
	
			return result;
		} else {
			return null;
		}
	}
}
