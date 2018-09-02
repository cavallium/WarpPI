package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureSine;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;

public class BlockSine extends BlockParenthesisAbstract {
	public BlockSine() {
		super("SIN");
	}

	@Override
	public Feature toFeature(MathContext context) throws Error {
		final Function cont = getNumberContainer().toFunction(context);
		return new FeatureSine(cont);
	}
}
