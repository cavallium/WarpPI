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

	public InlineInputContainer(InputContext ic) {
		super(ic);
	}

	public InlineInputContainer(InputContext ic, boolean small) {
		super(ic, small);
	}

	public InlineInputContainer(InputContext ic, boolean small, int minWidth, int minHeight) {
		super(ic, small, minWidth, minHeight);
	}

	@Override
	public Block parseChar(char c) {
		return new BlockChar(c);
	}
}
