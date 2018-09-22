package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule8
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * -a/-b = a/b
 *
 * @author Andrea Cavalli
 *
 */
public class FractionsRule8 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule8";
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
		if (f instanceof Division) {
			final MathContext root = f.getMathContext();
			final Division div = (Division) f;
			if (div.getParameter1() instanceof Multiplication && ((Multiplication) div.getParameter1()).isNegative())
				if (div.getParameter2() instanceof Multiplication && ((Multiplication) div.getParameter2()).isNegative()) {
					final ObjectArrayList<Function> result = new ObjectArrayList<>();
					result.add(new Division(root, ((Multiplication) div.getParameter1()).toPositive(), ((Multiplication) div.getParameter2()).toPositive()));
					return result;
				}
		}

		return null;
	}
}
