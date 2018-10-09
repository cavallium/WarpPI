package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class BlockExponentialNotation extends BlockPower {
	private int bw;
	private int bh;

	public BlockExponentialNotation() {
		super();
	}
	
	/**
	 * Copy
	 * @param old
	 * @param ic
	 */
	private BlockExponentialNotation(final TreeContainer parent, BlockExponentialNotation old, InputContext ic) {
		super(parent, old, ic);
		this.bw = old.bw;
		this.bh = old.bh;
	}

	@Override
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawStringLeft(x, y + height - bh, "ℯ℮");
		super.draw(ge, r, x + bw, y, caret);
	}

	@Override
	public void recomputeDimensions() {
		super.recomputeDimensions();
		bw = (int) (BlockContainer.getDefaultCharWidth(small) * 1.5);
		bh = BlockContainer.getDefaultCharHeight(small);
		width += bw;
	}
	
	@Override
	public BlockExponentialNotation clone(final TreeContainer parent, InputContext ic) {
		return new BlockExponentialNotation(parent, this, ic);
	}
}
