package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureLogarithm;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockLogarithm extends Block implements IParenthesis {

	private final BlockContainer containerBase;
	private final BlockContainer containerNumber;

	private final String prefix = "log";
	private int prw;
	private int bw;
	private int bh;
	private int bl;
	private int chw;
	private int chh;
	@SuppressWarnings("unused")
	private int schh;
	private int nmbh;
	private int toph;

	public BlockLogarithm() {
		containerBase = new BlockContainer(this, true);
		containerNumber = new BlockContainer(this, false);
		recomputeDimensions();
	}

	public BlockLogarithm(final ObjectArrayList<Block> blocks) {
		containerBase = new BlockContainer(this, true);
		containerNumber = new BlockContainer(this, false, blocks);
		recomputeDimensions();
	}

	private BlockLogarithm(BlockLogarithm old, InputContext ic) {
		containerBase = old.containerBase.clone(ic);
		containerNumber = old.containerNumber.clone(ic);
		recomputeDimensions();
	}

	@Override
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		if (prefix != null) {
			r.glDrawStringLeft(x + 1, y + line - chh / 2, prefix);
		}
		r.glDrawCharLeft(x + bw + prw, y + toph, '╭');
		r.glDrawCharLeft(x + bw + prw, y + toph + nmbh - chh, '╰');
		if (small) {
			r.glFillColor(x + bw + prw + 1, y + toph + 5, 1, nmbh - 4 * 2);
			r.glFillColor(x + width - 3, y + toph + 5, 1, nmbh - 4 * 2);
		} else {
			r.glFillColor(x + bw + prw + 3, y + toph + 6, 2, nmbh - 6 * 2);
			r.glFillColor(x + width - 5, y + toph + 6, 2, nmbh - 6 * 2);
		}
		r.glDrawCharLeft(x + width - chw, y + toph, '╮');
		r.glDrawCharLeft(x + width - chw, y + toph + nmbh - chh, '╯');
		r.glColor(BlockContainer.getDefaultColor());
		containerBase.draw(ge, r, x + prw, y + line + chh / 2 - bl, caret);
		r.glColor(BlockContainer.getDefaultColor());
		containerNumber.draw(ge, r, x + bw + prw + chw, y + toph, caret);
	}

	@Override
	public boolean putBlock(final Caret caret, final Block newBlock) {
		boolean added = false;
		added = added | containerBase.putBlock(caret, newBlock);
		added = added | containerNumber.putBlock(caret, newBlock);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	@Override
	public boolean delBlock(final Caret caret) {
		boolean removed = false;
		removed = removed | containerBase.delBlock(caret);
		removed = removed | containerNumber.delBlock(caret);
		if (removed) {
			recomputeDimensions();
		}
		return removed;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		BlockReference<?> bl = null;
		bl = containerBase.getBlock(caret);
		if (bl != null) {
			return bl;
		}
		bl = containerNumber.getBlock(caret);
		return bl;
	}

	@Override
	public void recomputeDimensions() {
		if (prefix == null) {
			prw = 0;
		} else {
			prw = 1 + BlockContainer.getDefaultCharWidth(small) * prefix.length();
		}
		bw = containerBase.getWidth();
		bh = containerBase.getHeight();
		bl = containerBase.getLine();
		chw = BlockContainer.getDefaultCharWidth(small);
		chh = BlockContainer.getDefaultCharHeight(small);
		schh = BlockContainer.getDefaultCharHeight(true);
		width = prw + bw + chw + containerNumber.getWidth() + chw + 3;
		nmbh = containerNumber.getHeight();
		final int nl = containerNumber.getLine();
		if (bl > nmbh) {
			toph = bl - nmbh;
			line = toph + nl;
			if (bl + bh - bl > toph + nmbh) {
				height = bl + bh - bl;
			} else {
				height = toph + nmbh;
			}
		} else {
			toph = 0;
			line = toph + nl;
			if (nmbh + bh - bl > toph + nmbh) {
				height = nmbh + bh - bl;
			} else {
				height = toph + nmbh;
			}
		}
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		containerBase.setSmall(small);
		containerNumber.setSmall(small);
		recomputeDimensions();
	}

	public BlockContainer getBaseContainer() {
		return containerBase;
	}

	public BlockContainer getNumberContainer() {
		return containerNumber;
	}

	@Override
	public int computeCaretMaxBound() {
		return containerBase.computeCaretMaxBound() + containerNumber.computeCaretMaxBound();
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function base = getBaseContainer().toFunction(context);
		final Function number = getNumberContainer().toFunction(context);
		return new FeatureLogarithm(base, number);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		ObjectArrayList<Block> output = containerBase.getContent();
		output.addAll(containerNumber.getContent());
		return output;
	}


	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(containerBase);
		output.add(containerNumber);
		return output;
	}

	@Override
	public BlockLogarithm clone(InputContext ic) {
		return new BlockLogarithm(this, ic);
	}

}
