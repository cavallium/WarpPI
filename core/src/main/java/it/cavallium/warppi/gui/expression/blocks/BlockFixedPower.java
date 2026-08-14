package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeaturePowerChar;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** A non-editable exponent used by x³ and x⁻¹. */
public final class BlockFixedPower extends Block {
	private final String exponent;

	public BlockFixedPower(final String exponent) {
		if (exponent == null || exponent.isEmpty())
			throw new IllegalArgumentException("An exponent is required");
		this.exponent = exponent;
		recomputeDimensions();
	}

	private BlockFixedPower(final TreeContainer parent, final BlockFixedPower old) {
		super(parent, old);
		exponent = old.exponent;
	}

	@Override
	public int getCaretDeltaPositionAfterCreation() {
		return 1;
	}

	@Override
	public void draw(final DisplayOutputDevice display, final Renderer renderer, final int x, final int y,
			final Caret caret) {
		BlockContainer.getDefaultFont(true).use(display);
		renderer.glColor(BlockContainer.getDefaultColor());
		renderer.glDrawStringLeft(x, y, exponent);
	}

	@Override public boolean insertBlock(final Caret caret, final Block newBlock) { return false; }
	@Override public boolean deleteBlock(final Caret caret) { return false; }
	@Override public BlockReference<?> getBlock(final Caret caret) { return null; }
	@Override public int computeCaretMaxBound() { return 0; }
	@Override public ObjectArrayList<Block> getInnerBlocks() { return null; }
	@Override public ObjectArrayList<BlockContainer> getInnerContainers() { return null; }

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(true) * exponent.length();
		height = BlockContainer.getDefaultCharHeight(true) + BlockContainer.getDefaultCharHeight(small) - 3;
		line = BlockContainer.getDefaultCharHeight(true)
				+ BlockContainer.getDefaultCharHeight(small) / 2 - 3;
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		recomputeDimensions();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		return new FeaturePowerChar(new it.cavallium.warppi.math.functions.Number(context, exponent));
	}

	@Override
	public BlockFixedPower clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockFixedPower(parent, this);
	}
}
