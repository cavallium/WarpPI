package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureParenthesis;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockParenthesis extends BlockParenthesisAbstract {
	public BlockParenthesis() {}

	public BlockParenthesis(final ObjectArrayList<Block> blocks) {
		super(blocks);
	}

	private BlockParenthesis(BlockParenthesis old, InputContext ic) {
		super(old, ic);
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function cont = getNumberContainer().toFunction(context);
		return new FeatureParenthesis(cont);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		return getNumberContainer().getContent();
	}

	@Override
	public int getInnerContainersCount() {
		return 1;
	}

	@Override
	public BlockParenthesis clone(InputContext ic) {
		return new BlockParenthesis(this, ic);
	}

}
