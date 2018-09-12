package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.JokeRule
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Joke
 *
 * 
 * @author Andrea Cavalli
 *
 */
public class JokeRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Joke";
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
	public ObjectArrayList<Function> execute(Function f) {
		return null;
	}
}
