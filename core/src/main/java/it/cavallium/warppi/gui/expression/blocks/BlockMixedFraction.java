package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureMixedFraction;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** A Casio-style mixed-fraction input template: whole, numerator, denominator. */
public final class BlockMixedFraction extends Block {
	private final BlockContainer whole;
	private final BlockContainer numerator;
	private final BlockContainer denominator;
	private int fractionTop;
	private int fractionWidth;
	private int numeratorPadding;
	private int denominatorPadding;

	public BlockMixedFraction() {
		whole = new BlockContainer(this, false);
		numerator = new BlockContainer(this, false);
		denominator = new BlockContainer(this, false);
		recomputeDimensions();
	}

	private BlockMixedFraction(final TreeContainer parent, final BlockMixedFraction old,
			final InputContext inputContext) {
		super(parent, old);
		whole = old.whole.clone(this, inputContext);
		numerator = old.numerator.clone(this, inputContext);
		denominator = old.denominator.clone(this, inputContext);
		recomputeDimensions();
	}

	public BlockContainer getWholeContainer() { return whole; }
	public BlockContainer getNumeratorContainer() { return numerator; }
	public BlockContainer getDenominatorContainer() { return denominator; }

	@Override
	public void draw(final DisplayOutputDevice display, final Renderer renderer, final int x, final int y,
			final Caret caret) {
		whole.draw(display, renderer, x, y + line - whole.getLine(), caret);
		final int fractionX = x + whole.getWidth() + 2;
		numerator.draw(display, renderer, fractionX + 1 + numeratorPadding, y + fractionTop, caret);
		renderer.glColor(BlockContainer.getDefaultColor());
		renderer.glDrawLine(fractionX, y + line, fractionX + fractionWidth - 1, y + line);
		denominator.draw(display, renderer, fractionX + 1 + denominatorPadding,
				y + fractionTop + numerator.getHeight() + 3, caret);
	}

	@Override
	public boolean insertBlock(final Caret caret, final Block block) {
		final boolean inserted = whole.insertBlock(caret, block)
				| numerator.insertBlock(caret, block) | denominator.insertBlock(caret, block);
		if (inserted)
			recomputeDimensions();
		return inserted;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		final boolean deleted = whole.deleteBlock(caret)
				| numerator.deleteBlock(caret) | denominator.deleteBlock(caret);
		if (deleted)
			recomputeDimensions();
		return deleted;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		BlockReference<?> block = whole.getBlock(caret);
		if (block == null)
			block = numerator.getBlock(caret);
		return block != null ? block : denominator.getBlock(caret);
	}

	@Override
	public void recomputeDimensions() {
		final int numeratorWidth = numerator.getWidth();
		final int denominatorWidth = denominator.getWidth();
		fractionWidth = Math.max(numeratorWidth, denominatorWidth) + 4;
		numeratorPadding = Math.max(0, (denominatorWidth - numeratorWidth) / 2);
		denominatorPadding = Math.max(0, (numeratorWidth - denominatorWidth) / 2);
		final int fractionLine = numerator.getHeight() + 1;
		line = Math.max(whole.getLine(), fractionLine);
		fractionTop = line - fractionLine;
		width = whole.getWidth() + 2 + fractionWidth;
		height = Math.max(line + whole.getHeight() - whole.getLine(),
				fractionTop + numerator.getHeight() + 3 + denominator.getHeight());
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		whole.setSmall(small);
		numerator.setSmall(small);
		denominator.setSmall(small);
		recomputeDimensions();
	}

	@Override
	public int computeCaretMaxBound() {
		return whole.computeCaretMaxBound() + numerator.computeCaretMaxBound()
				+ denominator.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function wholeFunction = whole.toFunction(context);
		final Function numeratorFunction = numerator.toFunction(context);
		final Function denominatorFunction = denominator.toFunction(context);
		return new FeatureMixedFraction(wholeFunction, numeratorFunction, denominatorFunction);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		final ObjectArrayList<Block> blocks = whole.getContent();
		blocks.addAll(numerator.getContent());
		blocks.addAll(denominator.getContent());
		return blocks;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		return new ObjectArrayList<>(new BlockContainer[] { whole, numerator, denominator });
	}

	@Override
	public BlockMixedFraction clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockMixedFraction(parent, this, inputContext);
	}
}
