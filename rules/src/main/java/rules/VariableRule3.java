package rules;
/*
SETTINGS: (please don't move this part)
 PATH=VariableRule3
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Variable rule
 * x+ax=(a+1)*x (a,b NUMBER; x VARIABLES)
 * 
 * @author Andrea Cavalli
 *
 */
public class VariableRule3 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "VariableRule3";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.REDUCTION;
	}

	/* Rule function
	   Returns:
	     - null if it's not executable on the function "f"
	     - An ObjectArrayList<Function> if it did something
	*/

	@Override
	public ObjectArrayList<Function> execute(Function f) {
		boolean isExecutable = false;
		if (f instanceof Sum || f instanceof Subtraction) {
			FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter2() instanceof Multiplication) {
				FunctionOperator m2 = (FunctionOperator) fnc.getParameter2();
				if (m2.getParameter2().equals(fnc.getParameter1())) {
					isExecutable = true;
				}
			}
		}

		if (isExecutable) {
			FunctionOperator fnc = (FunctionOperator) f;
			MathContext root = fnc.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator m2 = (FunctionOperator) fnc.getParameter2();
			Function a = m2.getParameter1();
			Function x = fnc.getParameter1();

			Function rets;
			if (fnc instanceof Sum) {
				rets = new Sum(root, new Number(root, 1), a);
			} else {
				rets = new Subtraction(root, new Number(root, 1), a);
			}

			Function retm = new Multiplication(root, rets, x);
			result.add(retm);
			return result;
		} else {
			return null;
		}
	}
}
