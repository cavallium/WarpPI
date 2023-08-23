package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.CaretState;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.*;
import it.cavallium.warppi.math.MathematicalSymbols;

public class NormalInputContainer extends InputContainer {

	@Deprecated()
	/**
	 * Use NormalInputContainer(InputContext) instead
	 */
	public NormalInputContainer() {
		super();
	}

	public NormalInputContainer(final InputContext ic) {
		super(ic);
	}

	public NormalInputContainer(final InputContext ic, final boolean small) {
		super(ic, small);
	}

	public NormalInputContainer(final InputContext ic, final boolean small, final int minWidth, final int minHeight) {
		super(ic, small, minWidth, minHeight);
	}

	/**
	 * Copy
	 * @param userInput
	 * @param ic
	 */
	public NormalInputContainer(InputContainer old, InputContext ic) {
		super(old, ic);
	}

	@Override
	public Block parseChar(final char c) {
		switch (c) {
			case MathematicalSymbols.DIVISION:
				return new BlockDivision();
			case MathematicalSymbols.SQUARE_ROOT:
				return new BlockSquareRoot();
			case MathematicalSymbols.PARENTHESIS_OPEN:
				return new BlockParenthesis();
			case MathematicalSymbols.PARENTHESIS_CLOSE:
				return null;
			case MathematicalSymbols.POWER:
				return new BlockPower();
			case MathematicalSymbols.POWER_OF_TWO:
				return new BlockPower2();
			case MathematicalSymbols.MULTIPLICATION:
			case MathematicalSymbols.SUM:
			case MathematicalSymbols.SUM_SUBTRACTION:
			case MathematicalSymbols.SUBTRACTION:
				return new BlockChar(c);
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '.':
				return new BlockNumericChar(c);
			case MathematicalSymbols.SINE:
				return new BlockSine();
			case MathematicalSymbols.LOGARITHM:
				return new BlockLogarithm();
			case MathematicalSymbols.PI:
				return new BlockVariable(inputContext, c, true);
			case MathematicalSymbols.EULER_NUMBER:
				return new BlockVariable(inputContext, c, true);
			default:
				for (final char v : MathematicalSymbols.variables) {
					if (c == v) {
						return new BlockVariable(inputContext, c);
					}
				}
				return new BlockChar(c);
		}
	}

	@Override
	public void typeChar(final char c) {
		super.typeChar(c);
		switch (c) {
			case MathematicalSymbols.PARENTHESIS_CLOSE: {
				final BlockReference<?> ref = getSelectedBlock();
				if (ref == null) {
					break;
				} else {
					final Caret newCaret = new Caret(CaretState.HIDDEN, caret.getPosition());
					BlockContainer currentContainer;
					BlockReference<?> newRef = ref;
					int safeExit = 0;
					do {
						currentContainer = (BlockContainer) newRef.get().getParentContainer();
						final int initialRelativeIndex = currentContainer.getContent().indexOf(newRef.get());
						final int newIndex = newCaret.getPosition() + currentContainer.getContent().size() - initialRelativeIndex;
						newRef = getBlockAtCaretPosition(newIndex);
						newCaret.setPosition(newIndex);
						safeExit++;
					} while (newRef != null && newRef.get() instanceof IParenthesis == false && currentContainer != null && safeExit < 100000);
					if (safeExit >= 100000) {
						System.err.println("Error 0x001030: Infinite loop");
					}
					if (newRef != null) {
						moveTo(newCaret.getPosition());
					}
				}
				break;
			}
			case MathematicalSymbols.DIVISION: {
				@SuppressWarnings("unchecked")
				final BlockReference<BlockDivision> ref = (BlockReference<BlockDivision>) getSelectedBlock();
				@SuppressWarnings("unused")
				final BlockContainer parentContainer = ref.getContainer();
				BlockReference<?> currentBlock = ref;
				boolean groupedBefore = false;
				int before = 0;
				while (true) {
					currentBlock = currentBlock.getPreviousBlock();
					if (currentBlock == null) {
						break;
					}
					final Block b = currentBlock.get();
					if (b instanceof BlockNumericChar || b instanceof BlockVariable) {
						if (!groupedBefore) {
							groupedBefore = true;
						}
						before++;
					} else {
						break;
					}
				}
				if (groupedBefore) {
					moveLeft();
					for (int i = 0; i < before; i++) {
						final BlockReference<?> b = getSelectedBlock();
						del();
						moveRight();
						typeBlock(b.get());
						moveLeft();
						moveLeft();
					}
					for (int i = 0; i < before + 1; i++) {
						moveRight();
					}
					moveRight();// Move to the divisor
				}
				break;
			}
		}
	}
}
