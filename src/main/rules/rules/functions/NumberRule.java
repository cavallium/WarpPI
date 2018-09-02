package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.NumberRule
*/

import it.cavallium.warppi.ScriptUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.RulesManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.math.BigInteger;

/**
 * Number
 *
 * 
 * @author Andrea Cavalli
 *
 */
public class NumberRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Number";
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
		if (f instanceof Number) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			MathContext mathContext = f.getMathContext();
			if (mathContext.exactMode) {
				if (((Number) f).isInteger() == false) {
					Number divisor = new Number(mathContext, BigInteger.TEN.pow(((Number) f).getNumberOfDecimalPlaces()));
					Function number = new Number(mathContext, ((Number) f).getTerm().multiply(divisor.getTerm()));
					Function div = new Division(mathContext, number, divisor);
					result.add(div);
					return result;
				}
			}
		}
		return null;
	}
}
