package it.cavallium.warppi.gui.expression;

import java.util.Arrays;

import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockVariable;
import it.cavallium.warppi.gui.expression.blocks.TreeContainer;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public abstract class ExtraMenu<T extends Block> implements KeyboardEventListener {

	private static final long serialVersionUID = -6944683477814944299L;

	public ExtraMenu(final T block) {
		this.block = block;
		this.location = new int[] { 0, 0 };
		this.width = 0;
		this.height = 0;
	}

	public ExtraMenu(final ExtraMenu<T> old, final T newBlock) {
		this.block = newBlock;
		this.location = Arrays.copyOf(old.location, old.location.length);
		this.width = old.width;
		this.height = old.height;
	}

	public final T block;
	protected int width;
	protected int height;
	protected int[] location;

	public abstract void draw(GraphicEngine ge, Renderer r, Caret caret);

	public abstract void open();

	public abstract void close();

	public boolean beforeRender(final float delta, final Caret caret) {
		final int[] l = caret.getLastLocation();
		final int[] cs = caret.getLastSize();
		location[0] = l[0] - block.getWidth() / 2 - width / 2;
		location[1] = l[1] + cs[1];
		return false;
	}

	public abstract ExtraMenu<T> clone(final TreeContainer parent, InputContext ic);

	public abstract ExtraMenu<T> clone(T newBlockVariable);

}
