package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.ExpressionRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Expression
 * (x) = x
 *
 * @author Andrea Cavalli
 *
 */
public class ExpressionRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Expression";
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
		if (f instanceof Expression) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			result.add(f.getParameter(0));
			return result;
		}
		return null;
	}
}
