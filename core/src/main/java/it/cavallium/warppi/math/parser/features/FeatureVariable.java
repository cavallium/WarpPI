package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;

public class FeatureVariable extends FeatureChar {

	public V_TYPE varType;

	public FeatureVariable(final char ch, final V_TYPE varType) {
		super(ch);
		this.varType = varType;
	}

	@Override
	public Function toFunction(final MathContext context) {
		return new Variable(context, ch, varType);
	}
}
