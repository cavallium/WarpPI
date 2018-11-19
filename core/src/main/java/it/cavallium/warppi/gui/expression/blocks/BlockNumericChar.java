package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.InputContext;

public class BlockNumericChar extends BlockChar {

	public BlockNumericChar(final char ch) {
		super(ch);
	}

	private BlockNumericChar(final TreeContainer parent, BlockNumericChar old, InputContext ic) {
		super(parent, old, ic);
	}

	@Override
	public BlockNumericChar clone(final TreeContainer parent, InputContext ic) {
		return new BlockNumericChar(parent, this, ic);
	}
}
