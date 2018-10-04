package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureSine;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockSine extends BlockParenthesisAbstract {
	public BlockSine() {
		super("SIN");
	}

	private BlockSine(BlockSine old, InputContext ic) {
		super(old, ic);
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function cont = getNumberContainer().toFunction(context);
		return new FeatureSine(cont);
	}

	@Override
	public Block clone(InputContext ic) {
		return new BlockSine(this, ic);
	}
}
