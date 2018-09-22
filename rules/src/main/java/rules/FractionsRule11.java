package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule11
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Fractions rule
 * a / (b / c) = (a * c) / b
 *
 * @author Andrea Cavalli
 *
 */
public class FractionsRule11 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule11";
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
	public ObjectArrayList<Function> execute(final Function f) {
		boolean isExecutable = false;
		if (f instanceof Division) {
			final FunctionOperator fnc = (FunctionOperator) f;
			@SuppressWarnings("unused")
			Function a;
			@SuppressWarnings("unused")
			Function c;
			FunctionOperator div2;
			if (fnc.getParameter2() instanceof Division) {
				div2 = (FunctionOperator) fnc.getParameter2();
				a = fnc.getParameter1();
				c = div2.getParameter2();
				isExecutable = true;
			} else
				isExecutable = false;
		}
		if (isExecutable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final FunctionOperator fnc = (FunctionOperator) f;
			Function a;
			Function b;
			Function c;

			final FunctionOperator div2 = (FunctionOperator) fnc.getParameter2();

			a = fnc.getParameter1();
			b = div2.getParameter1();
			c = div2.getParameter2();
			result.add(new Division(fnc.getMathContext(), new Multiplication(fnc.getMathContext(), a, c), b));

			return result;
		} else
			return null;
	}
}
