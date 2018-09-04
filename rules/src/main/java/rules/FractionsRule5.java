package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule5
*/

import java.math.BigDecimal;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Fractions rule
 * (a / b) ^ -c = (b / a) ^ c
 * 
 * @author Andrea Cavalli
 *
 */
public class FractionsRule5 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule5";
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
	public ObjectArrayList<Function> execute(Function f) throws Error {
		boolean isExecutable = false;
		if (f instanceof Power) {
			FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Division) {
				if (fnc.getParameter2() instanceof Multiplication && ((FunctionOperator) fnc.getParameter2()).getParameter1().equals(new Number(f.getMathContext(), -1))) {
					isExecutable = true;
				} else if (fnc.getParameter2() instanceof Number) {
					Number n2 = (Number) fnc.getParameter2();
					if (n2.getTerm().compareTo(BigDecimal.ZERO) < 0) {
						isExecutable = true;
					}
				}
			}
		}

		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			FunctionOperator fnc = (FunctionOperator) f;
			Function a = ((FunctionOperator) fnc.getParameter1()).getParameter1();
			Function b = ((FunctionOperator) fnc.getParameter1()).getParameter2();
			Function c;
			if (fnc.getParameter2() instanceof Multiplication) {
				c = ((FunctionOperator) fnc.getParameter2()).getParameter2();
			} else {
				c = ((Number) fnc.getParameter2()).multiply(new Number(root, "-1"));
			}
			Function dv = new Division(root, b, a);
			Function pow = new Power(root, dv, c);
			result.add(pow);
			return result;
		} else {
			return null;
		}
	}
}
