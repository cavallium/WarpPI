package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule1
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Fractions rule
 * 0 / a = 0
 *
 * @author Andrea Cavalli
 *
 */
public class FractionsRule1 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule1";
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
	public ObjectArrayList<Function> execute(final Function f) {
		boolean isExecutable = false;
		final MathContext root = f.getMathContext();
		if (f instanceof Division) {
			final FunctionOperator fnc = (FunctionOperator) f;
			if (fnc.getParameter1() instanceof Number) {
				final Function numb1 = fnc.getParameter1();
				if (numb1.equals(new Number(root, 0)))
					if (fnc.getParameter2() instanceof Number) {
						final Function numb2 = fnc.getParameter2();
						if (numb2.equals(new Number(root, 0)) == false)
							isExecutable = true;
					} else
						isExecutable = true;
			}
		}

		if (isExecutable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(new Number(f.getMathContext(), 0));
			return result;
		} else
			return null;
	}
}