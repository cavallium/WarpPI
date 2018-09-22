package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule6
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * a ^ -1 = 1/a
 *
 * @author Andrea Cavalli
 *
 */
public class FractionsRule6 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule6";
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
		if (f instanceof Power) {
			final MathContext root = f.getMathContext();
			final Power pow = (Power) f;
			if (pow.getParameter2() instanceof Number) {
				final Function numb = pow.getParameter2();
				if (numb.equals(new Number(root, -1)))
					isExecutable = true;
			}
		}

		if (isExecutable) {
			final MathContext root = f.getMathContext();
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Function a = new Division(root, new Number(root, 1), ((Power) f).getParameter1());
			result.add(a);
			return result;
		} else
			return null;
	}
}
