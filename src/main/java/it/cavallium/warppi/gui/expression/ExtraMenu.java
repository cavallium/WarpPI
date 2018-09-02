package it.cavallium.warppi.gui.expression;

import java.io.Serializable;

import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public abstract class ExtraMenu<T extends Block> implements Serializable, KeyboardEventListener {

	private static final long serialVersionUID = -6944683477814944299L;

	public ExtraMenu(T block) {
		this.block = block;
		this.location = new int[] { 0, 0 };
		this.width = 0;
		this.height = 0;
	}

	public final T block;
	protected int width;
	protected int height;
	protected int[] location;

	public abstract void draw(GraphicEngine ge, Renderer r, Caret caret);

	public abstract void open();

	public abstract void close();

	public boolean beforeRender(float delta, Caret caret) {
		final int[] l = caret.getLastLocation();
		final int[] cs = caret.getLastSize();
		location[0] = l[0] - block.getWidth() / 2 - width / 2;
		location[1] = l[1] + cs[1];
		return false;
	}

}
