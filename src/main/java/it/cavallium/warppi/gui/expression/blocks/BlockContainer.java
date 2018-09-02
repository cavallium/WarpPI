package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.Errors;
import it.cavallium.warppi.PlatformUtils;
import it.cavallium.warppi.gui.GraphicalElement;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.CaretState;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.MathParser;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockContainer implements GraphicalElement {

	private static boolean initialized = false;

	private int minWidth;
	private int minHeight;
	private final ObjectArrayList<Block> content;
	private boolean small;
	private int width;
	private int height;
	private int line;
	public final boolean withBorder;
	private boolean autoMinimums;

	public BlockContainer() {
		this(false, BlockContainer.getDefaultCharWidth(false), BlockContainer.getDefaultCharHeight(false), true);
		autoMinimums = true;
	}

	public BlockContainer(boolean small) {
		this(small, BlockContainer.getDefaultCharWidth(small), BlockContainer.getDefaultCharHeight(small), true);
		autoMinimums = true;
	}

	public BlockContainer(boolean small, ObjectArrayList<Block> content) {
		this(small, BlockContainer.getDefaultCharWidth(small), BlockContainer.getDefaultCharHeight(small), content, true);
		autoMinimums = true;
	}

	public BlockContainer(boolean small, boolean withBorder) {
		this(small, BlockContainer.getDefaultCharWidth(small), BlockContainer.getDefaultCharHeight(small), withBorder);
		autoMinimums = true;
	}

	public BlockContainer(boolean small, int minWidth, int minHeight, boolean withBorder) {
		this(small, minWidth, minHeight, new ObjectArrayList<>(), withBorder);
		autoMinimums = false;
	}

	public BlockContainer(boolean small, int minWidth, int minHeight, ObjectArrayList<Block> content, boolean withBorder) {
		this.small = small;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		this.withBorder = withBorder;
		for (final Block b : content) {
			if (b.isSmall() != small) {
				b.setSmall(small);
			}
		}
		this.content = content;
		recomputeDimensions();
	}

	public void addBlock(int position, Block b) {
		addBlockUnsafe(position, b);
		recomputeDimensions();
	}

	public void addBlockUnsafe(int position, Block b) {
		if (b.isSmall() != small) {
			b.setSmall(small);
		}
		if (position >= content.size()) {
			content.add(b);
		} else {
			content.add(position, b);
		}
	}

	public void appendBlock(Block b) {
		appendBlockUnsafe(b);
		recomputeDimensions();
	}

	public void appendBlockUnsafe(Block b) {
		if (b.isSmall() != small) {
			b.setSmall(small);
		}
		content.add(b);
	}

	public void removeBlock(Block b) {
		removeBlockUnsafe(b);
		recomputeDimensions();
	}

	public void removeBlockUnsafe(Block b) {
		content.remove(b);
	}

	public void removeAt(int i) {
		content.remove(i);
		recomputeDimensions();
	}

	public BlockReference<?> getBlockAt(int i) {
		final Block b = content.get(i);
		return new BlockReference<>(b, i, this);
	}

	public int getSize() {
		return content.size();
	}

	public void clear() {
		content.clear();
		recomputeDimensions();
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
	 * @param caret
	 *            Position of the caret.
	 */
	public void draw(GraphicEngine ge, Renderer r, int x, int y, Caret caret) {
		int paddingX = 1;

		if (caret.getRemaining() == 0) {
			if (content.size() > 0) {
				BlockContainer.drawCaret(ge, r, caret, small, x, y + line - content.get(0).line, content.get(0).height);
			} else {
				BlockContainer.drawCaret(ge, r, caret, small, x, y, height);
			}
		}

		if (withBorder && content.size() == 0) {
			r.glColor(BlockContainer.getDefaultColor());
			r.glDrawLine(x + paddingX, y, x + paddingX + width - 1, y);
			r.glDrawLine(x + paddingX, y, x + paddingX, y + height - 1);
			r.glDrawLine(x + paddingX + width - 1, y, x + paddingX + width - 1, y + height - 1);
			r.glDrawLine(x + paddingX, y + height - 1, x + paddingX + width - 1, y + height - 1);
		} else {
			for (final Block b : content) {
				caret.skip(1);
				b.draw(ge, r, x + paddingX, y + line - b.line, caret);
				paddingX += b.getWidth();
				if (caret.getRemaining() == 0) {
					BlockContainer.drawCaret(ge, r, caret, small, x + paddingX, y + line - b.line, b.height);
				}
				paddingX += 1;
			}
		}
		caret.skip(1);
	}

	public boolean putBlock(Caret caret, Block newBlock) {
		boolean added = false;

		if (caret.getRemaining() == 0) {
			addBlock(0, newBlock);
			added = true;
		}

		int pos = 0;
		for (final Block b : content) {
			caret.skip(1);
			pos++;
			added = added | b.putBlock(caret, newBlock);
			if (caret.getRemaining() == 0) {
				addBlock(pos, newBlock);
				added = true;
			}
		}
		caret.skip(1);
		if (added) {
			recomputeDimensions();
		}
		return added;
	}

	public boolean delBlock(Caret caret) {
		boolean removed = false;

		int pos = 0;
		for (final Block b : content) {
			caret.skip(1);
			pos++;
			final int deltaCaret = caret.getRemaining();
			removed = removed | b.delBlock(caret);
			if (caret.getRemaining() == 0 || (removed == false && deltaCaret >= 0 && caret.getRemaining() < 0)) {
				removeAt(pos - 1);
				caret.setPosition(caret.getPosition() - deltaCaret);
				removed = true;
			}
		}
		caret.skip(1);
		if (removed) {
			recomputeDimensions();
		}
		return removed;
	}

	public BlockReference<?> getBlock(Caret caret) {
		BlockReference<?> block = null;

		int pos = 0;
		for (final Block b : content) {
			caret.skip(1);
			pos++;
			final int deltaCaret = caret.getRemaining();

			block = b.getBlock(caret);
			if (block != null) {
				return block;
			}
			if (caret.getRemaining() == 0 || (deltaCaret >= 0 && caret.getRemaining() < 0)) {
				block = getBlockAt(pos - 1);
				return block;
			}
		}
		caret.skip(1);
		return block;
	}

	@Override
	public void recomputeDimensions() {
		int l = 0; //Line
		int w = 0; //Width
		int h2 = 0; //Height under the line. h = h2 + l
		int h = 0; //Height

		for (final Block b : content) {
			w += b.getWidth() + 1;
			final int bl = b.getLine();
			final int bh = b.getHeight();
			final int bh2 = bh - bl;
			if (bl > l) {
				l = bl;
			}
			if (bh2 > h2) {
				h2 = bh2;
			}
		}

		if (content.size() > 0) {
			w -= 1;
		}

		h = h2 + l;

		line = l;
		if (w > minWidth) {
			width = w;
		} else {
			width = minWidth;
		}
		if (h > minHeight) {
			height = h;
		} else {
			height = minHeight;
			line = height / 2;
		}
	}

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

	private static final BinaryFont[] defFonts = new BinaryFont[2];
	private static final int[] defFontSizes = new int[4];
	private static final int defColor = 0xFF000000;

	public static void initializeFonts(BinaryFont big, BinaryFont small) {
		defFonts[0] = big;
		defFonts[1] = small;
		defFontSizes[0] = big.getCharacterWidth();
		defFontSizes[1] = big.getCharacterHeight();
		defFontSizes[2] = small.getCharacterWidth();
		defFontSizes[3] = small.getCharacterHeight();
		initialized = true;
	}

	public static BinaryFont getDefaultFont(boolean small) {
		checkInitialized();
		return defFonts[small ? 1 : 0];
	}

	public static int getDefaultColor() {
		return defColor;
	}

	public static int getDefaultCharWidth(boolean b) {
		checkInitialized();
		return defFontSizes[b ? 2 : 0];
	}

	public static int getDefaultCharHeight(boolean b) {
		checkInitialized();
		return defFontSizes[b ? 3 : 1];
	}

	public static void drawCaret(GraphicEngine ge, Renderer r, Caret caret, boolean small, int x, int y, int height) {
		if (caret.getState() == CaretState.VISIBLE_ON) {
			r.glColor(getDefaultColor());
			r.glFillColor(x, y, small ? 2 : 3, height);
			caret.setLastLocation(x, y);
			caret.setLastSize(small ? 2 : 3, height);
		}
	}

	public void setSmall(boolean small) {
		this.small = small;
		if (autoMinimums) {
			minWidth = BlockContainer.getDefaultCharWidth(small);
			minHeight = BlockContainer.getDefaultCharHeight(small);
		}
		for (final Block b : content) {
			b.setSmall(small);
		}
		recomputeDimensions();
	}

	public ObjectArrayList<Block> getContent() {
		return content.clone();
	}

	private static void checkInitialized() {
		if (!initialized) {
			PlatformUtils.throwNewExceptionInInitializerError("Please initialize BlockContainer by running the method BlockContainer.initialize(...) first!");
		}
	}

	public int computeCaretMaxBound() {
		int maxpos = 0;
		for (final Block b : content) {
			maxpos += 1 + b.computeCaretMaxBound();
		}
		return maxpos + 1;
	}

	public Function toFunction(MathContext context) throws Error {
		final ObjectArrayList<Block> blocks = getContent();
		final ObjectArrayList<Feature> blockFeatures = new ObjectArrayList<>();

		for (final Block block : blocks) {
			final Feature blockFeature = block.toFeature(context);
			if (blockFeature == null) {
				throw new Error(Errors.NOT_IMPLEMENTED, "The block " + block.getClass().getSimpleName() + " isn't a known Block");
			}
			blockFeatures.add(blockFeature);
		}

		final Function result = MathParser.joinFeatures(context, blockFeatures);
		return result;
	}

}