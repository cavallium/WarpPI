package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.VariableRule
*/

import org.nevec.rjm.BigDecimalMath;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Variable
 * a = n
 *
 * @author Andrea Cavalli
 *
 */
public class VariableRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Variable";
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
	public ObjectArrayList<Function> execute(final Function f) throws Error {
		if (f instanceof Variable) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Character variable = ((Variable) f).getChar();
			final MathContext mathContext = f.getMathContext();
			if (mathContext.exactMode == false)
				if (variable.equals(MathematicalSymbols.PI)) {
					//a = n
					result.add(new Number(mathContext, BigDecimalMath.pi(new java.math.MathContext(Utils.scale, Utils.scaleMode2))));
					return result;
				}
		}
		return null;
	}
}
