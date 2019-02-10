package it.cavallium.warppi.math.rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.NumberRule
*/

import java.math.BigInteger;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
	public ObjectArrayList<Function> execute(final Function f) {
		if (f instanceof Number) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final MathContext mathContext = f.getMathContext();
			if (mathContext.exactMode)
				if (((Number) f).isInteger() == false) {
					final int decimalPlaces = ((Number) f).getNumberOfDecimalPlaces();
					final int decimalDigits = decimalPlaces + 1;
					if (decimalDigits < Utils.maxAutoFractionDigits) {
						final Number divisor = new Number(mathContext, BigInteger.TEN.pow(decimalPlaces));
						final Function number = new Number(mathContext, ((Number) f).getTerm().multiply(divisor.getTerm()));
						final Function div = new Division(mathContext, number, divisor);
						result.add(div);
						return result;
					}
				}
		}
		return null;
	}
}
