package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;

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
		switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '.':
				return new BlockNumericChar(c);
			default:
				return new BlockChar(c);
		}
	}
}
