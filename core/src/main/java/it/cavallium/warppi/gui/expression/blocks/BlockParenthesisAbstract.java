package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class BlockParenthesisAbstract extends Block implements IParenthesis {

	private final BlockContainer containerNumber;

	private final String prefix;
	private int prw;
	private int chw;
	private int chh;

	protected BlockParenthesisAbstract(final String prefix) {
		containerNumber = new BlockContainer(this, false);
		this.prefix = prefix;

		recomputeDimensions();
	}

	public BlockParenthesisAbstract() {
		containerNumber = new BlockContainer(this, false);
		prefix = null;
		recomputeDimensions();
	}

	/**
	 * Copy
	 * @param old
	 * @param ic
	 */
	BlockParenthesisAbstract(final TreeContainer parent, BlockParenthesisAbstract old, InputContext ic) {
		super(parent, old);
		containerNumber = old.containerNumber.clone(this, ic);
		prefix = old.prefix;
		prw = old.prw;
		chw = old.chw;
		chh = old.chh;
	}

	public BlockParenthesisAbstract(final ObjectArrayList<Block> blocks) {
		containerNumber = new BlockContainer(this, false, blocks);
		prefix = null;
		recomputeDimensions();
	}

	@Override
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y, final Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		if (prefix != null) {
			r.glDrawStringLeft(x + 1, y + line - chh / 2, prefix);
		}
		r.glDrawCharLeft(x + prw, y, '╭');
		r.glDrawCharLeft(x + prw, y + height - chh, '╰');
		if (small) {
			r.glFillColor(x + prw + 1, y + 5, 1, height - 4 * 2);
			r.glFillColor(x + width - 3, y + 5, 1, height - 4 * 2);
		} else {
			r.glFillColor(x + prw + 3, y + 6, 2, height - 6 * 2);
			r.glFillColor(x + width - 5, y + 6, 2, height - 6 * 2);
		}
		r.glDrawCharLeft(x + width - chw, y, '╮');
		r.glDrawCharLeft(x + width - chw, y + height - chh, '╯');
		containerNumber.draw(ge, r, x + prw + chw, y, caret);
	}

	@Override
	public boolean putBlock(final Caret caret, final Block newBlock) {
		boolean added = false;
		added = added | containerNumber.putBlock(caret, newBlock);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	@Override
	public boolean delBlock(final Caret caret) {
		boolean removed = false;
		removed = removed | containerNumber.delBlock(caret);
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
		if (prefix == null) {
			prw = 0;
		} else {
			prw = 1 + BlockContainer.getDefaultCharWidth(small) * prefix.length() + 2;
		}
		chw = BlockContainer.getDefaultCharWidth(small);
		chh = BlockContainer.getDefaultCharHeight(small);
		width = prw + chw + containerNumber.getWidth() + chw + 3;
		height = containerNumber.getHeight();
		line = containerNumber.getLine();
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
	public abstract Feature toFeature(MathContext context) throws Error;


	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		return containerNumber.getContent();
	}


	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		ObjectArrayList<BlockContainer> output = new ObjectArrayList<>();
		output.add(containerNumber);
		return output;
	}
	
}
