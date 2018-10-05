package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureDivision;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
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

	private BlockDivision(BlockDivision old, InputContext ic) {
		containerUp = old.containerUp.clone(ic);
		containerDown = old.containerDown.clone(ic);
		recomputeDimensions();
	}

	@Override
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		containerUp.draw(ge, r, x + 1 + paddingLeftUpper, y, caret);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawLine(x, y + h1 + 1, x + width - 1, y + h1 + 1);
		containerDown.draw(ge, r, x + 1 + paddingLeftLower, y + h1 + 3, caret);
	}

	@Override
	public boolean putBlock(final Caret caret, final Block newBlock) {
		boolean added = false;
		added = added | containerUp.putBlock(caret, newBlock);
		added = added | containerDown.putBlock(caret, newBlock);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	@Override
	public boolean delBlock(final Caret caret) {
		boolean removed = false;
		removed = removed | containerUp.delBlock(caret);
		removed = removed | containerDown.delBlock(caret);
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
	public BlockDivision clone(InputContext ic) {
		return new BlockDivision(this, ic);
	}
}
