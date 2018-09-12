package rules;
/*
SETTINGS: (please don't move this part)
 PATH=NumberRule2
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Number rule
 * a * 1 = a
 * 
 * @author Andrea Cavalli
 *
 */
public class NumberRule2 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "NumberRule2";
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
		boolean isExecutable = false;
		if (f instanceof Multiplication) {
			MathContext root = f.getMathContext();
			Multiplication mult = (Multiplication) f;
			if (mult.getParameter1() instanceof Number) {
				Function numb = mult.getParameter1();
				if (numb.equals(new Number(root, 1))) {
					isExecutable = true;
				}
			}
			if (mult.getParameter2() instanceof Number) {
				Function numb = mult.getParameter2();
				if (numb.equals(new Number(root, 1))) {
					isExecutable = true;
				}
			}
		}

		if (isExecutable) {
			MathContext root = f.getMathContext();
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			Function a = null;
			boolean aFound = false;
			Multiplication mult = (Multiplication) f;
			if (aFound == false & mult.getParameter1() instanceof Number) {
				Function numb = mult.getParameter1();
				if (numb.equals(new Number(root, 1))) {
					a = mult.getParameter2();
					aFound = true;
				}
			}
			if (aFound == false && mult.getParameter2() instanceof Number) {
				Function numb = mult.getParameter2();
				if (numb.equals(new Number(root, 1))) {
					a = mult.getParameter1();
					aFound = true;
				}
			}

			result.add(a);
			return result;
		} else {
			return null;
		}
	}
}
