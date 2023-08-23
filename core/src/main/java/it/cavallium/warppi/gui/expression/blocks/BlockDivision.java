package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureDivision;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockDivision extends Block {

	private final BlockContainer containerUp;
	private final BlockContainer containerDown;

	private int paddingLeftUpper;
	private int paddingLeftLower;
	private int h1;

	public BlockDivision() {
		containerUp = new BlockContainer(this, false);
		containerDown = new BlockContainer(this, false);
		recomputeDimensions();
	}

	private BlockDivision(final TreeContainer parent, BlockDivision old, InputContext ic) {
		super(parent, old);
		containerUp = old.containerUp.clone(this, ic);
		containerDown = old.containerDown.clone(this, ic);
		paddingLeftLower = old.paddingLeftLower;
		paddingLeftUpper = old.paddingLeftUpper;
		h1 = old.h1;
		System.out.println(String.join(",", ""+h1, ""+old.h1, ""+line, ""+old.line));
	}

	@Override
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		containerUp.draw(ge, r, x + 1 + paddingLeftUpper, y, caret);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawLine(x, y + h1 + 1, x + width - 1, y + h1 + 1);
		containerDown.draw(ge, r, x + 1 + paddingLeftLower, y + h1 + 3, caret);
	}

	@Override
	public boolean appendBlock(final Caret caret, final Block newBlock, boolean splitAdjacent) {
		boolean added = false;
		added = added | containerUp.appendBlock(caret, newBlock, splitAdjacent);
		added = added | containerDown.appendBlock(caret, newBlock, splitAdjacent);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		boolean removed = false;
		removed = removed | containerUp.deleteBlock(caret);
		removed = removed | containerDown.deleteBlock(caret);
		if (removed) {
			recomputeDimensions();
		}
		return removed;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		BlockReference<?> bl = null;
		bl = containerUp.getBlock(caret);
		if (bl != null) {
			return bl;
		}
		bl = containerDown.getBlock(caret);
		return bl;
	}

	@Override
	public void recomputeDimensions() {
		final int w1 = containerUp.getWidth();
		final int w2 = containerDown.getWidth();
		final int h1 = containerUp.getHeight();
		final int h2 = containerDown.getHeight();
		width = (w1 > w2 ? w1 : w2) + 4;
		height = h1 + 3 + h2;
		line = h1 + 1;
		this.h1 = h1;
		if (w1 != w2) {
			if (w1 > w2) {
				paddingLeftUpper = 0;
				paddingLeftLower = (w1 - w2) / 2;
			} else {
				paddingLeftUpper = (w2 - w1) / 2;
				paddingLeftLower = 0;
			}
		} else {
			paddingLeftUpper = 0;
			paddingLeftLower = 0;
		}
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		containerUp.setSmall(small);
		containerDown.setSmall(small);
		recomputeDimensions();
	}

	public BlockContainer getUpperContainer() {
		return containerUp;
	}

	public BlockContainer getLowerContainer() {
		return containerDown;
	}

	@Override
	public int computeCaretMaxBound() {
		return containerUp.computeCaretMaxBound() + containerDown.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function upper = getUpperContainer().toFunction(context);
		final Function lower = getLowerContainer().toFunction(context);
		return new FeatureDivision(upper, lower);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		ObjectArrayList<Block> output = containerUp.getContent();
		output.addAll(containerDown.getContent());
		return output;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(containerUp);
		output.add(containerDown);
		return output;
	}

	@Override
	public BlockDivision clone(final TreeContainer parent, InputContext ic) {
		return new BlockDivision(parent, this, ic);
	}
}
