package it.cavallium.warppi.gui.expression;

import java.util.HashMap;

import it.cavallium.warppi.gui.expression.blocks.BlockVariable;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;

public class InputContext {
	public final HashMap<Character, V_TYPE> variableTypes;
	public BlockVariable variableTypeDirtyID = null;

	public InputContext() {
		variableTypes = new HashMap<>();
		variableTypes.put(MathematicalSymbols.PI, V_TYPE.CONSTANT);
		variableTypes.put(MathematicalSymbols.EULER_NUMBER, V_TYPE.CONSTANT);
	}

	public InputContext(final HashMap<Character, V_TYPE> variableTypes) {
		this.variableTypes = variableTypes;
	}

	public InputContext(InputContext ic) {
		this.variableTypes = new HashMap<>();
		this.variableTypes.putAll(ic.variableTypes);
		this.variableTypeDirtyID = ic.variableTypeDirtyID;
	}
}
