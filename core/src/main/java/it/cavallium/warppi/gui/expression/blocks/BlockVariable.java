package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.ExtraMenu;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.parser.features.FeatureVariable;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockVariable extends Block {

	private final InputContext ic;
	private final char ch;
	private final VariableMenu menu;
	private V_TYPE type;
	private int color;
	private boolean mustRefresh = true;
	private BlockVariable typeDirtyID;
	private final boolean typeLocked;

	public BlockVariable(final InputContext ic, final char ch) {
		this(ic, ch, false);
	}

	public BlockVariable(final InputContext ic, final char ch, final boolean typeLocked) {
		this.ic = ic;
		this.ch = ch;
		type = V_TYPE.VARIABLE;
		color = 0xFF304ffe;
		typeDirtyID = this;
		this.typeLocked = typeLocked;
		menu = typeLocked ? null : new VariableMenu(this);
		retrieveValue();
		recomputeDimensions();
	}

	private BlockVariable(final TreeContainer parent, BlockVariable old, InputContext ic) {
		super(parent, old);
		this.ic = ic;
		this.ch = old.ch;
		type = old.type;
		color = old.color;
		typeDirtyID = old.typeDirtyID;
		this.typeLocked = old.typeLocked;
		menu = old.menu == null ? null : new VariableMenu(old.menu, this);
		mustRefresh = old.mustRefresh;
	}

	private void retrieveValue() {
		type = ic.variableTypes.get(ch);
		if (type == null) {
			type = V_TYPE.VARIABLE;
		}
		typeDirtyID = ic.variableTypeDirtyID;
		if (menu != null) {
			menu.mustRefreshMenu = true;
		}
		mustRefresh = true;
	}

	public void pushValue() {
		if (ic.variableTypeDirtyID != this) {
			typeDirtyID = this;
			ic.variableTypeDirtyID = this;
		} else {
			typeDirtyID = null;
			ic.variableTypeDirtyID = null;
		}
		ic.variableTypes.put(ch, type);
		System.out.println("push:" + type.toString());
	}

	@Override
	public void draw(final DisplayOutputDevice ge, final Renderer r, final int x, final int y, final Caret caret) {
		if (ic.variableTypeDirtyID != typeDirtyID) {
			retrieveValue();
		}
		if (mustRefresh) {
			mustRefresh = false;
			switch (type) {
				case VARIABLE:
					color = 0xFF304ffe;
					break;
				case CONSTANT:
					color = typeLocked ? 0xFF000000 : 0xFF35913F;
					break;
				case SOLUTION:
				default:
					color = 0xFFf50057;
					break;
			}
		}

		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(color);
		r.glDrawCharLeft(x, y, ch);
	}

	@Override
	public boolean appendBlock(final Caret caret, final Block newBlock, boolean splitAdjacent) {
		return false;
	}

	@Override
	public boolean deleteBlock(final Caret caret) {
		return false;
	}

	@Override
	public BlockReference<?> getBlock(final Caret caret) {
		return null;
	}

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(small);
		height = BlockContainer.getDefaultCharHeight(small);
		line = height / 2;
	}

	@Override
	public void setSmall(final boolean small) {
		this.small = small;
		recomputeDimensions();
	}

	public char getChar() {
		return ch;
	}

	@Override
	public int computeCaretMaxBound() {
		return 0;
	}

	@Override
	public ExtraMenu<?> getExtraMenu() {
		return menu;
	}

	public class VariableMenu extends ExtraMenu<BlockVariable> {

		String text = "";
		boolean mustRefreshMenu = true;

		public VariableMenu(final BlockVariable var) {
			super(var);
		}

		private VariableMenu(VariableMenu old, BlockVariable newBlockVariable) {
			super(old, newBlockVariable);
			this.mustRefreshMenu = old.mustRefreshMenu;
			this.text = new String(old.text);
		}

		@Override
		public void open() {

		}

		@Override
		public void close() {}

		@Override
		public boolean onKeyPressed(final KeyPressedEvent k) {
			switch (k.getKey()) {
				case LEFT:
				case UP:
					switch (block.type) {
						case VARIABLE:
							block.type = V_TYPE.SOLUTION;
							break;
						case CONSTANT:
							block.type = V_TYPE.VARIABLE;
							break;
						case SOLUTION:
						default:
							block.type = V_TYPE.CONSTANT;
							break;
					}
					break;
				case RIGHT:
				case DOWN:
				case EQUAL:
				case SIMPLIFY:
					switch (block.type) {
						case VARIABLE:
							block.type = V_TYPE.CONSTANT;
							break;
						case CONSTANT:
							block.type = V_TYPE.SOLUTION;
							break;
						case SOLUTION:
						default:
							block.type = V_TYPE.VARIABLE;
							break;
					}
					break;
				default:
					return false;
			}

			block.pushValue();
			mustRefresh = true;
			mustRefreshMenu = true;
			return true;
		}

		@Override
		public boolean onKeyReleased(final KeyReleasedEvent k) {
			return false;
		}

		@Override
		public boolean beforeRender(final float delta, final Caret caret) {
			if (mustRefreshMenu) {
				mustRefreshMenu = false;
				text = block.type.toString();
				final BinaryFont f = BlockContainer.getDefaultFont(true);
				width = 7 + f.getStringWidth(text) + 7;
				height = 2 + f.getCharacterHeight() + 2;

				super.beforeRender(delta, caret);
				return true;
			}
			return false;
		}

		@Override
		public void draw(final DisplayOutputDevice ge, final Renderer r, final Caret caret) {
			r.glColor3f(1.0f, 1.0f, 1.0f);
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().guiSkin.use(ge);
			int popupX = location[0];
			int popupY = location[1];
			if (popupX < 0) {
				popupX = 0;
			}
			if (popupY < 0) {
				popupY = 0;
			}
			final int[] screenSize = ge.getDisplaySize();
			if (popupX + width >= screenSize[0]) {
				popupX = screenSize[0] - width - 1;
			}
			if (popupY + height >= screenSize[1]) {
				popupY = screenSize[1] - height - 1;
			}
			r.glFillRect(location[0] + width / 2 - 5, popupY + 1, 10, 5, 163, 16, 10, 5);
			r.glFillColor(popupX, popupY + 5, width, height);
			r.glFillColor(popupX + 2, popupY + 4, width - 4, height + 2);
			r.glFillColor(popupX - 1, popupY + 7, width + 2, height - 4);
			r.glFillRect(popupX + 2, popupY + 5 + height / 2 - 7 / 2, 4, 7, 160, 21, 4, 7);
			r.glFillRect(popupX + width - 2 - 4, popupY + 5 + height / 2 - 7 / 2, 4, 7, 172, 21, 4, 7);
			r.glColor(color);
			BlockContainer.getDefaultFont(true).use(ge);
			r.glDrawStringCenter(popupX + width / 2, popupY + 2 + 5, text);
		}

		@Override
		public VariableMenu clone(BlockVariable newBlockVariable) {
			return new VariableMenu(this, newBlockVariable);
		}

		@Override
		public VariableMenu clone(final TreeContainer parent, InputContext ic) {
			return new VariableMenu(this, block.clone(parent, ic));
		}

	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		return new FeatureVariable(ch, type);
	}

	@Override
	public ObjectArrayList<Block> getInnerBlocks() {
		return null;
	}

	@Override
	public ObjectArrayList<BlockContainer> getInnerContainers() {
		return null;
	}
	
	@Override
	public BlockVariable clone(final TreeContainer parent, InputContext ic) {
		return new BlockVariable(parent, this, ic);
	}
}
