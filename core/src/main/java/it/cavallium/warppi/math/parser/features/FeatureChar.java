package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;

public class FeatureChar implements Feature {

	public final char ch;

	public FeatureChar(final char ch) {
		this.ch = ch;
	}

	@Override
	public Function toFunction(final MathContext context) {
		return null;
	}

}
