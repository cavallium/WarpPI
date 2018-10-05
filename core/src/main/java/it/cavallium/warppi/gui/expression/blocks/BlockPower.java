package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeaturePowerChar;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockPower extends Block {

	private final BlockContainer containerExponent;

	public BlockPower() {
		containerExponent = new BlockContainer(this, true);
		recomputeDimensions();
	}

	private BlockPower(BlockPower old, InputContext ic) {
		this.containerExponent = old.containerExponent.clone(ic);
	}

	@Override
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(true).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		containerExponent.draw(ge, r, x, y, caret);
	}

	@Override
	public boolean putBlock(final Caret caret, final Block newBlock) {
		boolean added = false;
		added = added | containerExponent.putBlock(caret, newBlock);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}
	
	@Override
	public boolean delBlock(Caret caret) {
		boolean removed = false;
		removed = removed | containerExponent.delBlock(caret);
		if (removed) {
			recomputeDimensions();
		}
		return removed;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		return containerExponent.getBlock(caret);
	}

	@Override
	public void recomputeDimensions() {
		final int w2 = containerExponent.getWidth();
		final int h2 = containerExponent.getHeight();
		width = w2 + 1;
		height = h2 + BlockContainer.getDefaultCharHeight(small) - 3;
		line = h2 + BlockContainer.getDefaultCharHeight(small) / 2 - 3;
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		containerExponent.setSmall(true);
		recomputeDimensions();
	}

	public BlockContainer getExponentContainer() {
		return containerExponent;
	}

	@Override
	public int computeCaretMaxBound() {
		return containerExponent.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function exp = getExponentContainer().toFunction(context);
		return new FeaturePowerChar(exp);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		return this.containerExponent.getContent();
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(containerExponent);
		return output;
	}

	@Override
	public BlockPower clone(InputContext ic) {
		return new BlockPower(this, ic);
	}
}
