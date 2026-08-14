package it.cavallium.warppi.gui.expression.blocks;

import java.util.Objects;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.math.parser.features.FeaturePostfixFunction;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BlockPostfixFunction extends Block {
	private final ScientificFunctionKind kind;

	public BlockPostfixFunction(final ScientificFunctionKind kind) {
		this.kind = requirePostfixKind(kind);
		recomputeDimensions();
	}

	private BlockPostfixFunction(final TreeContainer parent, final BlockPostfixFunction old) {
		super(parent, old);
		kind = old.kind;
	}

	private static ScientificFunctionKind requirePostfixKind(final ScientificFunctionKind kind) {
		if (!Objects.requireNonNull(kind, "kind").isPostfix())
			throw new IllegalArgumentException(kind + " is not postfix");
		return kind;
	}

	@Override
	public void draw(final DisplayOutputDevice display, final Renderer renderer, final int x, final int y,
			final Caret caret) {
		BlockContainer.getDefaultFont(small).use(display);
		renderer.glColor(BlockContainer.getDefaultColor());
		renderer.glDrawStringLeft(x, y, kind.postfix());
	}

	@Override public boolean insertBlock(final Caret caret, final Block newBlock) { return false; }
	@Override public boolean deleteBlock(final Caret caret) { return false; }
	@Override public BlockReference<?> getBlock(final Caret caret) { return null; }

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(small) * kind.postfix().length();
		height = BlockContainer.getDefaultCharHeight(small);
		line = height / 2;
	}

	@Override public int computeCaretMaxBound() { return 0; }
	@Override public ObjectArrayList<Block> getInnerBlocks() { return null; }
	@Override public ObjectArrayList<BlockContainer> getInnerContainers() { return null; }

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		recomputeDimensions();
	}

	@Override
	public Feature toFeature(final MathContext context) {
		return new FeaturePostfixFunction(kind);
	}

	@Override
	public BlockPostfixFunction clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockPostfixFunction(parent, this);
	}
}
