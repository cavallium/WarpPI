package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureParenthesis;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockParenthesis extends BlockParenthesisAbstract {
	public BlockParenthesis() {
		super();
	}

	public BlockParenthesis(final ObjectArrayList<Block> blocks) {
		super(blocks);
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function cont = getNumberContainer().toFunction(context);
		return new FeatureParenthesis(cont);
	}

}
