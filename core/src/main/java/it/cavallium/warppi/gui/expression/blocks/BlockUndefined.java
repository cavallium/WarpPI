package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.parser.features.FeatureChar;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;

public class BlockUndefined extends Block {

	public BlockUndefined() {
		recomputeDimensions();
	}

	@Override
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawStringLeft(x, y, "UNDEFINED");
	}

	@Override
	public boolean putBlock(final Caret caret, final Block newBlock) {
		return false;
	}

	@Override
	public boolean delBlock(final Caret caret) {
		return false;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		return null;
	}

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultFont(small).getStringWidth("UNDEFINED");
		height = BlockContainer.getDefaultCharHeight(small);
		line = height / 2;
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		recomputeDimensions();
	}

	@Override
	public int computeCaretMaxBound() {
		return 0;
	}

	@Override
	public Feature toFeature(final MathContext context) {
		return new FeatureChar(MathematicalSymbols.UNDEFINED);
	}

}
