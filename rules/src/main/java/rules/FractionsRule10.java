package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule10
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
 * a/(-b) = -(a/b)
 * 
 * @author Andrea Cavalli
 *
 */
public class FractionsRule10 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule10";
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
		if (f instanceof Division) {
			MathContext root = f.getMathContext();
			Division div = (Division) f;
			if (div.getParameter2() instanceof Multiplication && ((Multiplication)div.getParameter2()).isNegative()) {
				ObjectArrayList<Function> result = new ObjectArrayList<>();
				result.add(Multiplication.newNegative(root, new Division(root, div.getParameter1(), ((Multiplication)div.getParameter2()).toPositive())));
				return result;
			}
		}
		
		return null;
	}
}
