package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.VariableRule
*/

import it.cavallium.warppi.Error;
import it.cavallium.warppi.ScriptUtils;
import it.cavallium.warppi.Utils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.RulesManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.nevec.rjm.BigDecimalMath;

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
	public ObjectArrayList<Function> execute(Function f) throws Error {
		if (f instanceof Variable) {
			ObjectArrayList<Function> result = new ObjectArrayList<>();
			Character variable = ((Variable) f).getChar();
			MathContext mathContext = f.getMathContext();
			if (mathContext.exactMode == false) {
				if (variable.equals(MathematicalSymbols.PI)) {
					//a = n
					result.add(new Number(mathContext, BigDecimalMath.pi(new java.math.MathContext(Utils.scale, Utils.scaleMode2))));
					return result;
				}
			}
		}
		return null;
	}
}
