package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule8
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
	public ObjectArrayList<Function> execute(Function f) {
		if (f instanceof Division) {
			MathContext root = f.getMathContext();
			Division div = (Division) f;
			if (div.getParameter1() instanceof Multiplication && ((Multiplication) div.getParameter1()).isNegative()) {
				if (div.getParameter2() instanceof Multiplication && ((Multiplication) div.getParameter2()).isNegative()) {
					ObjectArrayList<Function> result = new ObjectArrayList<>();
					result.add(new Division(root, ((Multiplication) div.getParameter1()).toPositive(), ((Multiplication) div.getParameter2()).toPositive()));
					return result;
				}
			}
		}

		return null;
	}
}
