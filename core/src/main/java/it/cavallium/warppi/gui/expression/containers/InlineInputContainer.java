package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;

public class InlineInputContainer extends InputContainer {

	private static final long serialVersionUID = 4307434049083324966L;

	@Deprecated()
	/**
	 * Use InlineInputContainer(InputContext) instead
	 */
	public InlineInputContainer() {
		super();
	}

	public InlineInputContainer(final InputContext ic) {
		super(ic);
	}

	public InlineInputContainer(final InputContext ic, final boolean small) {
		super(ic, small);
	}

	public InlineInputContainer(final InputContext ic, final boolean small, final int minWidth, final int minHeight) {
		super(ic, small, minWidth, minHeight);
	}

	@Override
	public Block parseChar(final char c) {
		return new BlockChar(c);
	}
}
