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
import it.cavallium.warppi.gui.expression.blocks.BlockReference;
import it.cavallium.warppi.gui.expression.layouts.InputLayout;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
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
	private CaretBounds cachedCaretBounds;
	private int cachedCaretPosition = -1;
	private int cachedCaretWidth = -1;
	private int cachedCaretHeight = -1;
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
		invalidateCaretMeasurement();
		if (b != null) {
			caret.resetRemaining();
			if (root.insertBlock(caret, b)) {
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
		invalidateCaretMeasurement();
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
		invalidateCaretMeasurement();
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
		invalidateCaretMeasurement();

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
		invalidateCaretMeasurement();
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
		invalidateCaretMeasurement();
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
		invalidateCaretMeasurement();
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

	public int getCaretPosition() {
		return caret.getPosition();
	}

	/**
	 * Measures the current structural caret without drawing it to the visible renderer.
	 * Coordinates are relative to this expression's top-left corner.
	 */
	public CaretBounds measureCaret(final DisplayOutputDevice display) {
		final int position = caret.getPosition();
		final int width = root.getWidth();
		final int height = root.getHeight();
		if (cachedCaretBounds != null && cachedCaretPosition == position
				&& cachedCaretWidth == width && cachedCaretHeight == height) {
			return cachedCaretBounds;
		}
		final Caret measuringCaret = new Caret(CaretState.VISIBLE_ON, position);
		root.draw(display, CARET_MEASURING_RENDERER, 0, 0, measuringCaret);
		final int[] location = measuringCaret.getLastLocation();
		final int[] size = measuringCaret.getLastSize();
		cachedCaretBounds = new CaretBounds(location[0], location[1], size[0], size[1]);
		cachedCaretPosition = position;
		cachedCaretWidth = width;
		cachedCaretHeight = height;
		return cachedCaretBounds;
	}

	private void invalidateCaretMeasurement() {
		cachedCaretBounds = null;
		cachedCaretPosition = -1;
		cachedCaretWidth = -1;
		cachedCaretHeight = -1;
	}

	public record CaretBounds(int x, int y, int width, int height) {}

	private static final Renderer CARET_MEASURING_RENDERER = new CaretMeasuringRenderer();

	private static final class CaretMeasuringRenderer implements Renderer {
		@Override public void glColor3i(final int r, final int g, final int b) {}
		@Override public void glColor(final int color) {}
		@Override public void glColor4i(final int red, final int green, final int blue, final int alpha) {}
		@Override public void glColor3f(final float red, final float green, final float blue) {}
		@Override public void glColor4f(final float red, final float green, final float blue, final float alpha) {}
		@Override public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {}
		@Override public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {}
		@Override public int glGetClearColor() { return 0; }
		@Override public void glClearColor(final int color) {}
		@Override public void glClear(final int screenWidth, final int screenHeight) {}
		@Override public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {}
		@Override public void glFillRect(final float x, final float y, final float width, final float height,
				final float uvX, final float uvY, final float uvWidth, final float uvHeight) {}
		@Override public void glFillColor(final float x, final float y, final float width, final float height) {}
		@Override public void glDrawCharLeft(final int x, final int y, final char character) {}
		@Override public void glDrawCharCenter(final int x, final int y, final char character) {}
		@Override public void glDrawCharRight(final int x, final int y, final char character) {}
		@Override public void glDrawStringLeft(final float x, final float y, final String text) {}
		@Override public void glDrawStringCenter(final float x, final float y, final String text) {}
		@Override public void glDrawStringRight(final float x, final float y, final String text) {}
		@Override public void glClearSkin() {}
		@Override public BinaryFont getCurrentFont() { return null; }
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
