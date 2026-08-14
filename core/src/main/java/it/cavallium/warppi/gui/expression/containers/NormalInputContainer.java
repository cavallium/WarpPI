package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.Caret;
import it.cavallium.warppi.gui.expression.CaretState;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockLogarithm;
import it.cavallium.warppi.gui.expression.blocks.BlockMixedFraction;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;
import it.cavallium.warppi.gui.expression.blocks.BlockParenthesis;
import it.cavallium.warppi.gui.expression.blocks.BlockPower;
import it.cavallium.warppi.gui.expression.blocks.BlockPower2;
import it.cavallium.warppi.gui.expression.blocks.BlockReference;
import it.cavallium.warppi.gui.expression.blocks.BlockSine;
import it.cavallium.warppi.gui.expression.blocks.BlockSquareRoot;
import it.cavallium.warppi.gui.expression.blocks.BlockTrigonometric;
import it.cavallium.warppi.gui.expression.blocks.BlockVariable;
import it.cavallium.warppi.gui.expression.blocks.IParenthesis;
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
			case MathematicalSymbols.COSINE:
				return new BlockTrigonometric(BlockTrigonometric.Kind.COSINE);
			case MathematicalSymbols.TANGENT:
				return new BlockTrigonometric(BlockTrigonometric.Kind.TANGENT);
			case MathematicalSymbols.ARC_SINE:
				return new BlockTrigonometric(BlockTrigonometric.Kind.ARC_SINE);
			case MathematicalSymbols.ARC_COSINE:
				return new BlockTrigonometric(BlockTrigonometric.Kind.ARC_COSINE);
			case MathematicalSymbols.ARC_TANGENT:
				return new BlockTrigonometric(BlockTrigonometric.Kind.ARC_TANGENT);
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
		if (c == MathematicalSymbols.DIVISION) {
			final BlockDivision division = new BlockDivision();
			if (!wrapExpressionToLeft(division, division.getUpperContainer(), 1))
				typeBlock(division);
			return;
		}
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
		}
	}

	/** Inserts the Casio-style mixed-fraction template, capturing a left prefix as its whole part. */
	public void typeMixedFraction() {
		final BlockMixedFraction fraction = new BlockMixedFraction();
		if (!wrapExpressionToLeft(fraction, fraction.getWholeContainer(), 1))
			typeBlock(fraction);
	}
}
