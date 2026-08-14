package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureRandomInteger;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BlockRandomInteger extends Block implements IParenthesis {
	private final BlockContainer minimum;
	private final BlockContainer maximum;

	public BlockRandomInteger() {
		minimum = new BlockContainer(this, false);
		maximum = new BlockContainer(this, false);
		recomputeDimensions();
	}

	private BlockRandomInteger(final TreeContainer parent, final BlockRandomInteger old,
			final InputContext inputContext) {
		super(parent, old);
		minimum = old.minimum.clone(this, inputContext);
		maximum = old.maximum.clone(this, inputContext);
	}

	public BlockContainer getMinimumContainer() { return minimum; }
	public BlockContainer getMaximumContainer() { return maximum; }

	@Override
	public void draw(final DisplayOutputDevice display, final Renderer renderer, final int x, final int y,
			final Caret caret) {
		final int charWidth = BlockContainer.getDefaultCharWidth(small);
		BlockContainer.getDefaultFont(small).use(display);
		renderer.glColor(BlockContainer.getDefaultColor());
		renderer.glDrawStringLeft(x, y, "RanInt(");
		minimum.draw(display, renderer, x + charWidth * 7, y, caret);
		renderer.glDrawStringLeft(x + charWidth * 7 + minimum.getWidth(), y, ",");
		maximum.draw(display, renderer, x + charWidth * 8 + minimum.getWidth(), y, caret);
		renderer.glDrawStringLeft(x + width - charWidth, y, ")");
	}

	@Override
	public boolean insertBlock(final Caret caret, final Block block) {
		final boolean inserted = minimum.insertBlock(caret, block) | maximum.insertBlock(caret, block);
		if (inserted) recomputeDimensions();
		return inserted;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		final boolean deleted = minimum.deleteBlock(caret) | maximum.deleteBlock(caret);
		if (deleted) recomputeDimensions();
		return deleted;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		final BlockReference<?> first = minimum.getBlock(caret);
		return first != null ? first : maximum.getBlock(caret);
	}

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(small) * 9 + minimum.getWidth() + maximum.getWidth();
		height = Math.max(minimum.getHeight(), maximum.getHeight());
		line = Math.max(minimum.getLine(), maximum.getLine());
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		minimum.setSmall(small);
		maximum.setSmall(small);
		recomputeDimensions();
	}

	@Override public int computeCaretMaxBound() {
		return minimum.computeCaretMaxBound() + maximum.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function first = minimum.toFunction(context);
		final Function second = maximum.toFunction(context);
		return new FeatureRandomInteger(first, second);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		final ObjectArrayList<Block> blocks = minimum.getContent();
		blocks.addAll(maximum.getContent());
		return blocks;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		return new ObjectArrayList<>(new BlockContainer[] { minimum, maximum });
	}

	@Override
	public BlockRandomInteger clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockRandomInteger(parent, this, inputContext);
	}
}
