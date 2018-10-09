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

	private BlockParenthesis(final TreeContainer parent, BlockParenthesis old, InputContext ic) {
		super(parent, old, ic);
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
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(getNumberContainer());
		return output;
	}

	@Override
	public BlockParenthesis clone(final TreeContainer parent, InputContext ic) {
		return new BlockParenthesis(parent, this, ic);
	}

}
