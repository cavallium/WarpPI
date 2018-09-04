package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class BlockExponentialNotation extends BlockPower {
	private int bw;
	private int bh;

	@Override
	public void draw(GraphicEngine ge, Renderer r, int x, int y, Caret caret) {
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
}
