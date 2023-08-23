package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.GraphicalElement;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.CaretState;
import it.cavallium.warppi.gui.expression.ExtraMenu;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockReference;
import it.cavallium.warppi.gui.expression.layouts.InputLayout;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class InputContainer implements GraphicalElement, InputLayout {
	protected BlockContainer root;
	protected Caret caret;
	private static final float CARET_DURATION = 0.5f;
	private float caretTime;
	private int maxPosition = 0;
	private boolean parsed = false;
	private ExtraMenu<?> extra;
	protected InputContext inputContext;

	public synchronized InputContext getInputContext() {
		return inputContext;
	}

	@Deprecated()
	/**
	 * Use InputContainer(InputContext) instead
	 */
	public InputContainer() {
		this(new InputContext());
	}

	public InputContainer(final InputContext ic) {
		this(ic, false);
	}

	public InputContainer(final InputContext ic, final boolean small) {
		this(ic, small, 0, 0);
	}

	public InputContainer(final InputContext ic, final boolean small, final int minWidth, final int minHeight) {
		inputContext = ic;
		caret = new Caret(CaretState.VISIBLE_ON, 0);
		root = new BlockContainer(null, small, false);
	}
	
	/**
	 * Copy
	 * @param old
	 * @param ic
	 */
	protected InputContainer(InputContainer old, InputContext ic) {
		this.caretTime = old.caretTime;
		this.extra = old.extra == null ? null : old.extra.clone(null, ic);
		this.maxPosition = old.maxPosition;
		this.caret = old.caret == null ? null : new Caret(old.caret);
		this.inputContext = ic;
		this.root = old.root == null ? null : old.root.clone(null, ic);
		this.parsed = old.parsed;
	}

	public void typeChar(final char c) {
		final Block b = parseChar(c);
		typeBlock(b);
	}

	public void typeBlock(final Block b) {
		if (b != null) {
			caret.resetRemaining();

			// todo: allow blocks to dinamically choose insert mode
			var splitAdjacent = b instanceof BlockDivision;

			if (root.appendBlock(caret, b, splitAdjacent)) {
				caret.setPosition(caret.getPosition() + b.getCaretDeltaPositionAfterCreation());
				maxPosition = root.computeCaretMaxBound();
				root.recomputeDimensions();
			}
			closeExtra();
		}
		caretTime = 0;
		caret.turnOn();
	}

	public void typeChar(final String c) {
		typeChar(c.charAt(0));
	}

	public void del() {
		caret.resetRemaining();
		if (root.deleteBlock(caret)) {
			root.recomputeDimensions();
		}
		if (caret.getPosition() > 0) {
			caret.setPosition(caret.getPosition() - 1);
			maxPosition = root.computeCaretMaxBound();
		}
		caret.turnOn();
		caretTime = 0;
		closeExtra();
	}

	public BlockReference<?> getSelectedBlock() {
		caret.resetRemaining();
		final BlockReference<?> selectedBlock = root.getBlock(caret);
		return selectedBlock;
	}

	public BlockReference<?> getBlockAtCaretPosition(final int i) {
		final BlockReference<?> selectedBlock = root.getBlock(new Caret(CaretState.HIDDEN, i));
		return selectedBlock;
	}

	public void moveLeft() {
		final int curPos = caret.getPosition();
		if (curPos > 0) {
			caret.setPosition(curPos - 1);
		} else {
			caret.setPosition(maxPosition - 1);
		}
		caret.turnOn();
		caretTime = 0;
		closeExtra();
	}

	public void moveRight(final int delta) {
		
		final int curPos = caret.getPosition();
		if (curPos + delta < maxPosition) {
			caret.setPosition(curPos + delta);
		} else {
			caret.setPosition(0);
		}
		caret.turnOn();
		caretTime = 0;
		closeExtra();
	}

	public void moveTo(final int position) {
		if (position < maxPosition) {
			caret.setPosition(position);
		} else {
			caret.setPosition(0);
		}
		caret.turnOn();
		caretTime = 0;
		closeExtra();
	}

	public void moveRight() {
		moveRight(1);
	}

	@Override
	public void recomputeDimensions() {
		root.recomputeDimensions();
	}

	@Override
	public int getWidth() {
		return root.getWidth();
	}

	@Override
	public int getHeight() {
		return root.getHeight();
	}

	@Override
	public int getLine() {
		return root.getLine();
	}

	/**
	 *
	 * @param delta
	 *            Time, in seconds
	 * @return true if something changed
	 */
	public boolean beforeRender(final float delta) {
		boolean somethingChanged = false;
		caretTime += delta;
		if (caretTime >= InputContainer.CARET_DURATION) {
			while (caretTime >= InputContainer.CARET_DURATION) {
				caretTime -= InputContainer.CARET_DURATION;
				caret.flipState();
				somethingChanged = true;
			}
		}

		if (extra != null) {
			somethingChanged = somethingChanged | extra.beforeRender(delta, caret);
		}

		return somethingChanged;
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
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y) {
		caret.resetRemaining();
		root.draw(ge, r, x, y, caret);
		if (extra != null) {
			extra.draw(ge, r, caret);
		}
	}

	public void clear() {
		caret = new Caret(CaretState.VISIBLE_ON, 0);
		root.clear();
		maxPosition = root.computeCaretMaxBound();
		recomputeDimensions();
	}

	public boolean isEmpty() {
		return maxPosition <= 1;
	}

	public int getCaretMaxPosition() {
		return maxPosition;
	}

	public void setCaretPosition(final int pos) {
		if (pos > 0 && pos < maxPosition) {
			caret.setPosition(pos);
		}
		caret.turnOn();
		caretTime = 0;
		closeExtra();
	}

	public void setParsed(final boolean parsed) {
		this.parsed = parsed;
	}

	public boolean isAlreadyParsed() {
		return parsed;
	}

	/**
	 * <strong>WARNING! DO NOT MODIFY THIS ARRAY!!!</strong>
	 *
	 * @return an arraylist representing the content
	 */
	public ObjectArrayList<Block> getContent() {
		return root.getContent();
	}

	public void toggleExtra() {
		if (extra == null) {
			final BlockReference<?> selectedBlock = getSelectedBlock();
			if (selectedBlock != null) {
				extra = selectedBlock.get().getExtraMenu();
				if (extra != null) {
					extra.open();
				}
			}
		} else {
			extra.close();
			extra = null;
		}
	}

	public void closeExtra() {
		if (extra != null) {
			extra.close();
			extra = null;
		}
	}

	public boolean isExtraOpened() {
		return extra != null;
	}

	public KeyboardEventListener getExtraKeyboardEventListener() {
		return extra;
	}

	public Function toFunction(final MathContext context) throws Error {
		return root.toFunction(context);
	}

}
