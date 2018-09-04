package rules;
/*
SETTINGS: (please don't move this part)
 PATH=FractionsRule7
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
 * a ^ -b = 1/(a^b)
 * 
 * @author Andrea Cavalli
 *
 */
public class FractionsRule7 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "FractionsRule7";
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
		boolean isExecutable = false;
		if (f instanceof Power) {
			MathContext root = f.getMathContext();
			Power pow = (Power) f;
			if (pow.getParameter2() instanceof Number) {
				Number numb = (Number) pow.getParameter2();
				if (numb.getTerm().compareTo(BigDecimal.ZERO) < 0) {
					ObjectArrayList<Function> result = new ObjectArrayList<>();
					Function a = new Division(root, new Number(root, 1), new Power(root, ((Power) f).getParameter1(), ((Number) ((Power) f).getParameter2()).multiply(new Number(root, -1))));
					result.add(a);
					return result;
				}
			} else if (pow.getParameter2() instanceof Multiplication && ((Multiplication) pow.getParameter2()).getParameter1().equals(new Number(root, -1))) {
				ObjectArrayList<Function> result = new ObjectArrayList<>();
				Function a = new Division(root, new Number(root, 1), new Power(root, ((Power) f).getParameter1(), ((Multiplication) ((Power) f).getParameter2()).getParameter2()));
				result.add(a);
				return result;
			}
		}

		return null;
	}
}
