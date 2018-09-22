package rules;
/*
SETTINGS: (please don't move this part)
 PATH=ExpandRule2
*/

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Expand rule
 * a(b+c)=ab+ac
 *
 * @author Andrea Cavalli
 *
 */
public class ExpandRule2 implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "ExpandRule2";
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
	public ObjectArrayList<Function> execute(final Function f) throws Error {
		boolean isExecutable = false;
		if (f instanceof Multiplication) {
			final Multiplication fnc = (Multiplication) f;
			if (fnc.getParameter1() instanceof Sum)
				isExecutable = true;
			else if (fnc.getParameter2() instanceof Sum)
				isExecutable = true;
			else
				isExecutable = false;
		}
		if (isExecutable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final MathContext root = f.getMathContext();

			final Multiplication fnc = (Multiplication) f;
			final Sum sum;
			final Function a;
			if (fnc.getParameter1() instanceof Sum) {
				sum = (Sum) fnc.getParameter1();
				a = fnc.getParameter2();
			} else if (fnc.getParameter2() instanceof Sum) {
				sum = (Sum) fnc.getParameter2();
				a = fnc.getParameter1();
			} else
				throw new Error(Errors.UNBALANCED_STACK);

			final Function b = sum.getParameter1();
			final Function c = sum.getParameter2();
			final Multiplication ab = new Multiplication(root, a, b);
			final Multiplication ac = new Multiplication(root, a, c);
			result.add(new Sum(root, ab, ac));
			return result;
		} else
			return null;

	}
}
