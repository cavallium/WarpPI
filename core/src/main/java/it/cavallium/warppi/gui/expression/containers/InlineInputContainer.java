package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockReference;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;

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
