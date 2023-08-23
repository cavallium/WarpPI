package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureChar;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockChar extends Block {

	private final char ch;

	public BlockChar(final char ch) {
		this.ch = ch;
		recomputeDimensions();
	}

	/**
	 * Copy
	 * @param b
	 * @param ic
	 */
	protected BlockChar(final TreeContainer parent, final BlockChar b, InputContext ic) {
		super(parent, b);
		this.ch = b.ch;
	}

	@Override
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawCharLeft(x, y, ch);
	}

	@Override
	public boolean appendBlock(final Caret caret, final Block newBlock, boolean splitAdjacent) {
		return false;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		return false;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		return null;
	}

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(small) - 1;
		height = BlockContainer.getDefaultCharHeight(small);
		line = height / 2;
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		recomputeDimensions();
	}

	public char getChar() {
		return ch;
	}

	@Override
	public int computeCaretMaxBound() {
		return 0;
	}

	@Override
	public Feature toFeature(final MathContext context) {
		return new FeatureChar(getChar());
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		return null;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		return null;
	}

	@Override
	public BlockChar clone(final TreeContainer parent, InputContext ic) {
		return new BlockChar(parent, this, ic);
	}

}
