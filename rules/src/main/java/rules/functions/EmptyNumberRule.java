package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.EmptyNumberRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * EmptyNumber
 *
 *
 * @author Andrea Cavalli
 *
 */
public class EmptyNumberRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "EmptyNumber";
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
		return null;
	}
}
