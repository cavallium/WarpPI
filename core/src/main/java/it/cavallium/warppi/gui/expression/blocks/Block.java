package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.GraphicalElement;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.ExtraMenu;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;

public abstract class Block implements TreeBlock, GraphicalElement {

	protected boolean small;
	protected int width;
	protected int height;
	protected int line;
	protected TreeContainer parent;

	/**
	 *
	 * @param r
	 *            Graphic Renderer class.
	 * @param x
	 *            Position relative to the window.
	 * @param y
	 *            Position relative to the window.
	 * @param small
	 */
	public abstract void draw(GraphicEngine ge, Renderer r, int x, int y, Caret caret);

	public abstract boolean putBlock(Caret caret, Block newBlock);

	public abstract boolean delBlock(Caret caret);

	public abstract BlockReference<?> getBlock(Caret caret);

	@Override
	public abstract void recomputeDimensions();

	public abstract int computeCaretMaxBound();

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getLine() {
		return line;
	}

	public int getCaretDeltaPositionAfterCreation() {
		return 1;
	}

	public boolean isSmall() {
		return small;
	}

	public abstract void setSmall(boolean small);

	public ExtraMenu<?> getExtraMenu() {
		return null;
	}

	public abstract Feature toFeature(MathContext context) throws Error;
	
	@Override
	public TreeContainer getParentContainer() {
		return parent;
	}
	
	@Override
	public boolean hasParent() {
		return parent != null;
	}

	public void setParent(TreeContainer parent) {
		this.parent = parent;
	}
}
