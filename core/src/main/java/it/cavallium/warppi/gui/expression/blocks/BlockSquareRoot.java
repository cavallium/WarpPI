package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureSquareRoot;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockSquareRoot extends Block {

	private final BlockContainer containerNumber;

	private int h1;

	public BlockSquareRoot() {
		containerNumber = new BlockContainer(this, false);
		recomputeDimensions();
	}

	private BlockSquareRoot(final TreeContainer parent, BlockSquareRoot old, InputContext ic) {
		super(parent, old);
		this.containerNumber = old.containerNumber.clone(this, ic);
		this.h1 = old.h1;
	}

	@Override
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawLine(x, y + height - 10 + 1, x, y + height - 10 + 2); // /
		r.glDrawLine(x + 1, y + height - 10, x + 1, y + height - 10 + 1); // /
		r.glDrawLine(x + 2, y + height - 10 + 2, x + 2, y + height - 10 + 6); // \
		r.glDrawLine(x + 3, y + height - 10 + 7, x + 3, y + height - 10 + 9); // \
		r.glDrawLine(x + 5, y + height - h1 - 1 - 2, x + width - 1, y + height - h1 - 1 - 2); // ----
		r.glDrawLine(x + 5, y + height - h1 - 1 - 2, x + 5, y + height - (h1 - 2) / 3f * 2f - 1); // |
		r.glDrawLine(x + 4, y + height - (h1 - 2) / 3f * 2f - 1, x + 4, y + height - (h1 - 2) / 3f - 1); // |
		r.glDrawLine(x + 3, y + height - (h1 - 2) / 3f - 1, x + 3, y + height - 1); // |
		containerNumber.draw(ge, r, x + 7, y + 3, caret);
	}

	@Override
	public boolean appendBlock(final Caret caret, final Block newBlock, boolean splitAdjacent) {
		boolean added = false;
		added = added | containerNumber.appendBlock(caret, newBlock, splitAdjacent);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		boolean removed = false;
		removed = removed | containerNumber.deleteBlock(caret);
		if (removed) {
			recomputeDimensions();
		}
		return removed;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		return containerNumber.getBlock(caret);
	}

	@Override
	public void recomputeDimensions() {
		final int w1 = containerNumber.getWidth();
		h1 = containerNumber.getHeight();
		final int l1 = containerNumber.getLine();
		width = 8 + w1 + 2;
		height = 3 + h1;
		line = 3 + l1;
		if (height < 9) {
			height = 9;
			line += 9 - (3 + h1);
		}
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		containerNumber.setSmall(small);
		recomputeDimensions();
	}

	public BlockContainer getNumberContainer() {
		return containerNumber;
	}

	@Override
	public int computeCaretMaxBound() {
		return containerNumber.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function contnt = getNumberContainer().toFunction(context);
		return new FeatureSquareRoot(contnt);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		ObjectArrayList<Block> output = containerNumber.getContent();
//		output.addAll();
		return output;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(containerNumber);
		return output;
	}

	@Override
	public BlockSquareRoot clone(final TreeContainer parent, InputContext ic) {
		return new BlockSquareRoot(parent, this, ic);
	}
}
