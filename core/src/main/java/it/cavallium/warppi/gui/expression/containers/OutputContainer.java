package it.cavallium.warppi.gui.expression.containers;

import java.io.Serializable;

import it.cavallium.warppi.gui.GraphicalElement;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.CaretState;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.layouts.OutputLayout;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class OutputContainer implements GraphicalElement, OutputLayout, Serializable {
	private static final long serialVersionUID = -5714825964892683571L;
	public final ObjectArrayList<BlockContainer> roots;
	private final Caret caret = new Caret(CaretState.HIDDEN, 0);

	public OutputContainer() {
		roots = new ObjectArrayList<>();
		roots.add(new BlockContainer(null));
	}

	public OutputContainer(final boolean small) {
		roots = new ObjectArrayList<>();
		roots.add(new BlockContainer(null, small));
	}

	public OutputContainer(final boolean small, final int minWidth, final int minHeight) {
		roots = new ObjectArrayList<>();
		roots.add(new BlockContainer(null, small));
	}

	public void setContentAsSingleGroup(final ObjectArrayList<Block> blocks) {
		roots.clear();
		final BlockContainer bcnt = new BlockContainer(null);
		for (final Block block : blocks)
			bcnt.appendBlockUnsafe(block);
		roots.add(bcnt);
		recomputeDimensions();
	}

	public void setContentAsMultipleGroups(final ObjectArrayList<ObjectArrayList<Block>> roots) {
		this.roots.clear();
		for (final ObjectArrayList<Block> blocks : roots) {
			final BlockContainer bcnt = new BlockContainer(null);
			for (final Block block : blocks)
				bcnt.appendBlockUnsafe(block);
			this.roots.add(bcnt);
		}
		recomputeDimensions();
	}

	public void setContentAsMultipleElements(final ObjectArrayList<Block> elems) {
		roots.clear();
		for (final Block block : elems) {
			final BlockContainer bcnt = new BlockContainer(null);
			bcnt.appendBlockUnsafe(block);
			roots.add(bcnt);
		}
		recomputeDimensions();
	}

	@Override
	public void recomputeDimensions() {
		for (final BlockContainer root : roots)
			root.recomputeDimensions();
	}

	@Override
	public int getWidth() {
		int maxw = 0;
		for (final BlockContainer root : roots) {
			final int w = root.getWidth();
			if (w > maxw)
				maxw = w;
		}
		return maxw;
	}

	@Override
	public int getHeight() {
		int h = 0;
		for (final BlockContainer root : roots)
			h += root.getHeight() + 2;
		if (h > 0)
			return h - 2;
		else
			return h;
	}

	@Override
	public int getLine() {
		return 0;
	}

	/**
	 *
	 * @param delta
	 *            Time, in seconds
	 */
	public void beforeRender(final double delta) {

	}

	/**
	 *
	 * @param ge
	 *            Graphic Engine class.
	 * @param r
	 *            Graphic Renderer class of <b>ge</b>.
	 * @param x
	 *            Position relative to the window.
	 * @param y
	 *            Position relative to the window.
	 */
	public void draw(final GraphicEngine ge, final Renderer r, final int x, final int y) {
		int offset = 0;
		for (final BlockContainer root : roots) {
			root.draw(ge, r, x, y + offset, caret);
			offset += root.getHeight() + 2;
		}
	}

	public void clear() {
		roots.clear();
		roots.add(new BlockContainer(null));
		recomputeDimensions();
	}

	public boolean isContentEmpty() {
		for (final BlockContainer root : roots) {
			final ObjectArrayList<Block> cnt = root.getContent();
			if (cnt != null && !cnt.isEmpty())
				return false;
		}
		return true;
	}
}
