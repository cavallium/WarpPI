package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.DivisionRule
*/

import java.math.BigInteger;
import java.util.LinkedList;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.ScriptUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Division
 * a/b = c
 *
 * @author Andrea Cavalli
 *
 */
public class DivisionRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Division";
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
		if (f instanceof Division) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final Function variable1 = ((FunctionOperator) f).getParameter1();
			final Function variable2 = ((FunctionOperator) f).getParameter2();
			final MathContext mathContext = f.getMathContext();
			if (variable1 instanceof Number && variable2 instanceof Number)
				if (mathContext.exactMode) {
					if (((Number) variable1).isInteger() && ((Number) variable2).isInteger()) {
						LinkedList<BigInteger> factors1, factors2, mcm;
						try {
							factors1 = ((Number) variable1).getFactors();
							factors2 = ((Number) variable2).getFactors();
							mcm = ScriptUtils.mcm(factors1, factors2);
						} catch (final Exception ex) {
							return null;
						}
						if (mcm.size() > 0) { //true if there is at least one common factor
							//divide by the common factor (ab/cb = a/c)
							BigInteger nmb1 = ((Number) variable1).getTerm().toBigIntegerExact();
							BigInteger nmb2 = ((Number) variable2).getTerm().toBigIntegerExact();
							for (final BigInteger integerNumber : mcm) {
								nmb1 = nmb1.divide(integerNumber);
								nmb2 = nmb2.divide(integerNumber);
							}
							result.add(new Division(mathContext, new Number(mathContext, nmb1), new Number(mathContext, nmb2)));
							return result;
						}
					}
				} else {
					//divide a by b (a/b = c)
					result.add(((Number) variable1).divide((Number) variable2));
					return result;
				}
		}
		return null;
	}
}
