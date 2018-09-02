package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule14
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
 * (a / b) * (c / d) = (a * c) / (b * d)
 * 
 * @author Andrea Cavalli
 *
 */
public class FractionsRule14 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule14";
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
			Function a;
			Function b;
			Function c;
			Function d;
			if (fnc.getParameter1() instanceof Division && fnc.getParameter2() instanceof Division) {
				FunctionOperator div1 = (FunctionOperator) fnc.getParameter1();
				FunctionOperator div2 = (FunctionOperator) fnc.getParameter2();
				a = div1.getParameter1();
				b = div1.getParameter2();
				c = div2.getParameter1();
				d = div2.getParameter2();
				isExecutable = true;
			} else if (fnc.getParameter1() instanceof Division) {
				FunctionOperator div1 = (FunctionOperator) fnc.getParameter1();
				a = div1.getParameter1();
				b = div1.getParameter2();
				c = fnc.getParameter2();
				isExecutable = true;
			} else if (fnc.getParameter2() instanceof Division) {
				FunctionOperator div2 = (FunctionOperator) fnc.getParameter2();
				a = fnc.getParameter1();
				c = div2.getParameter1();
				d = div2.getParameter2();
				isExecutable = true;
			}
		}
	
		if (isExecutable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator fnc = (FunctionOperator) f;
			Function a;
			Function b;
			Function c;
			Function d;
	
			if (fnc.getParameter1() instanceof Division && fnc.getParameter2() instanceof Division) {
				FunctionOperator div1 = (FunctionOperator) fnc.getParameter1();
				FunctionOperator div2 = (FunctionOperator) fnc.getParameter2();
				a = div1.getParameter1();
				b = div1.getParameter2();
				c = div2.getParameter1();
				d = div2.getParameter2();
				Function div = new Division(fnc.getMathContext(), new Multiplication(fnc.getMathContext(), a, c), new Multiplication(fnc.getMathContext(), b, d));
				result.add(div);
			} else if (fnc.getParameter1() instanceof Division) {
				FunctionOperator div1 = (FunctionOperator) fnc.getParameter1();
				a = div1.getParameter1();
				b = div1.getParameter2();
				c = fnc.getParameter2();
				Function div = new Division(fnc.getMathContext(), new Multiplication(fnc.getMathContext(), a, c), b);
				result.add(div);
			} else if (fnc.getParameter2() instanceof Division) {
				FunctionOperator div2 = (FunctionOperator) fnc.getParameter2();
				a = fnc.getParameter1();
				c = div2.getParameter1();
				d = div2.getParameter2();
				Function div = new Division(fnc.getMathContext(), new Multiplication(fnc.getMathContext(), a, c), d);
				result.add(div);
			}
			return result;
		} else {
			return null;
		}
	}
}
