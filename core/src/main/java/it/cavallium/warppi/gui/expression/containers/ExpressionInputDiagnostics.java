package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockExponentialNotation;
import it.cavallium.warppi.gui.expression.blocks.BlockLogarithm;
import it.cavallium.warppi.gui.expression.blocks.BlockMixedFraction;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;
import it.cavallium.warppi.gui.expression.blocks.BlockParenthesisAbstract;
import it.cavallium.warppi.gui.expression.blocks.BlockPower;
import it.cavallium.warppi.gui.expression.blocks.BlockPower2;
import it.cavallium.warppi.gui.expression.blocks.BlockSine;
import it.cavallium.warppi.gui.expression.blocks.BlockSquareRoot;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** Locates common syntax mistakes in the visual input tree. */
final class ExpressionInputDiagnostics {

	private ExpressionInputDiagnostics() {}

	static ExpressionInputDiagnostic diagnose(final BlockContainer root) {
		return diagnoseContainer(root, 0, "NOTHING TO CALCULATE", "ENTER AN EXPRESSION",
				"START TYPING AN EXPRESSION");
	}

	private static ExpressionInputDiagnostic diagnoseContainer(final BlockContainer container,
			final int baseCaretPosition, final String emptyTitle, final String emptyMessage,
			final String emptyCorrection) {
		final ObjectArrayList<Block> blocks = container.getContent();
		if (blocks.isEmpty()) {
			return insertion(baseCaretPosition, emptyTitle, emptyMessage, emptyCorrection);
		}

		boolean expectingValue = true;
		boolean sawValue = false;
		boolean decimalPointSeen = false;
		boolean digitSeenInNumber = false;
		int decimalPointPosition = baseCaretPosition;
		char pendingBinaryOperator = 0;
		int pendingBinaryPosition = baseCaretPosition;
		char pendingPrefixSign = 0;
		int pendingPrefixPosition = baseCaretPosition;
		char unaryMinus = 0;
		int unaryMinusPosition = baseCaretPosition;
		int equationCount = 0;
		int cursor = baseCaretPosition;
		Block previousBlock = null;

		for (final Block block : blocks) {
			final int positionAfterBlock = cursor + 1 + block.computeCaretMaxBound();
			if (!(block instanceof BlockNumericChar) && decimalPointSeen && !digitSeenInNumber) {
				return incompleteDecimal(decimalPointPosition);
			}

			if (block instanceof BlockNumericChar numeric) {
				final boolean continuingNumber = previousBlock instanceof BlockNumericChar;
				if (!continuingNumber) {
					decimalPointSeen = false;
					digitSeenInNumber = false;
				}
				if (numeric.getChar() == '.') {
					if (decimalPointSeen) {
						return previous(positionAfterBlock, "INVALID NUMBER",
								"ONLY ONE DECIMAL POINT IS ALLOWED",
								"DELETE THE UNDERLINED DOT");
					}
					decimalPointSeen = true;
					decimalPointPosition = positionAfterBlock;
				} else {
					digitSeenInNumber = true;
				}
				expectingValue = false;
				sawValue = true;
				pendingBinaryOperator = 0;
				pendingPrefixSign = 0;
				unaryMinus = 0;
			} else if (isPower(block)) {
				decimalPointSeen = false;
				if (!(block instanceof BlockExponentialNotation) && expectingValue) {
					return next(cursor, "INCOMPLETE POWER", "THE EXPONENT NEEDS A BASE",
							"TYPE THE BASE AT CURSOR");
				}
				final ExpressionInputDiagnostic childProblem = diagnosePower(block, cursor + 1);
				if (childProblem != null) {
					return childProblem;
				}
				expectingValue = false;
				sawValue = true;
				pendingBinaryOperator = 0;
				pendingPrefixSign = 0;
				unaryMinus = 0;
			} else if (block instanceof BlockChar characterBlock) {
				decimalPointSeen = false;
				final char character = characterBlock.getChar();
				if (isMinus(character)) {
					if (expectingValue) {
						if (unaryMinus != 0) {
							return missingBetween(cursor, unaryMinus, character);
						}
						unaryMinus = character;
						unaryMinusPosition = positionAfterBlock;
					} else {
						expectingValue = true;
						pendingBinaryOperator = character;
						pendingBinaryPosition = positionAfterBlock;
						unaryMinus = 0;
					}
				} else if (isBinaryOperator(character)) {
					if (character == MathematicalSymbols.EQUATION && ++equationCount > 1) {
						return previous(positionAfterBlock, "INVALID EQUATION", "ONLY ONE = IS ALLOWED",
								"DELETE THE UNDERLINED =");
					}
					if (expectingValue && isPrefixSign(character)) {
						if (unaryMinus != 0) {
							return missingBetween(cursor, unaryMinus, character);
						}
						pendingPrefixSign = character;
						pendingPrefixPosition = positionAfterBlock;
						previousBlock = block;
						cursor = positionAfterBlock;
						continue;
					}
					if (expectingValue) {
						if (blocks.size() == 1) {
							return next(cursor, incompleteTitle(character), operatorLabel(character)
									+ " NEEDS A VALUE ON EACH SIDE",
									"TYPE LEFT VALUE AT CURSOR");
						}
						if (unaryMinus != 0) {
							return missingBetween(cursor, unaryMinus, character);
						}
						if (pendingPrefixSign != 0) {
							return missingBetween(cursor, pendingPrefixSign, character);
						}
						if (pendingBinaryOperator != 0) {
							return missingBetween(cursor, pendingBinaryOperator, character);
						}
						if (!sawValue) {
							return next(cursor, incompleteTitle(character),
									operatorLabel(character) + " NEEDS A VALUE ON THE LEFT",
									"TYPE LEFT VALUE AT CURSOR");
						}
					}
					expectingValue = true;
					pendingBinaryOperator = character;
					pendingBinaryPosition = positionAfterBlock;
					pendingPrefixSign = 0;
					unaryMinus = 0;
				} else {
					return previous(positionAfterBlock, "UNSUPPORTED SYMBOL",
							"THIS SYMBOL CANNOT BE USED HERE",
							"DELETE THE UNDERLINED SYMBOL");
				}
			} else {
				decimalPointSeen = false;
				final ExpressionInputDiagnostic childProblem = diagnoseOperand(block, cursor + 1);
				if (childProblem != null) {
					return childProblem;
				}
				expectingValue = false;
				sawValue = true;
				pendingBinaryOperator = 0;
				pendingPrefixSign = 0;
				unaryMinus = 0;
			}

			previousBlock = block;
			cursor = positionAfterBlock;
		}

		if (decimalPointSeen && !digitSeenInNumber) {
			return incompleteDecimal(decimalPointPosition);
		}
		if (expectingValue) {
			if (unaryMinus != 0) {
				return previous(unaryMinusPosition, "INCOMPLETE EXPRESSION",
						operatorLabel(unaryMinus) + " NEEDS A VALUE ON THE RIGHT",
						"TYPE VALUE AT CURSOR");
			}
			if (pendingPrefixSign != 0) {
				return previous(pendingPrefixPosition, incompleteTitle(pendingPrefixSign),
						operatorLabel(pendingPrefixSign) + " NEEDS A VALUE ON THE RIGHT",
						"TYPE VALUE AT CURSOR");
			}
			if (pendingBinaryOperator != 0) {
				return previous(pendingBinaryPosition, incompleteTitle(pendingBinaryOperator),
						operatorLabel(pendingBinaryOperator) + " NEEDS A VALUE ON THE RIGHT",
						"TYPE RIGHT VALUE AT CURSOR");
			}
		}

		return null;
	}

	private static ExpressionInputDiagnostic diagnoseOperand(final Block block, final int childBase) {
		if (block instanceof BlockDivision division) {
			final ExpressionInputDiagnostic numerator = diagnoseContainer(division.getUpperContainer(), childBase,
					"INCOMPLETE FRACTION", "ENTER A NUMERATOR", "TYPE IT AT CURSOR");
			if (numerator != null) {
				return numerator;
			}
			return diagnoseContainer(division.getLowerContainer(),
					childBase + division.getUpperContainer().computeCaretMaxBound(), "INCOMPLETE FRACTION",
					"ENTER A DENOMINATOR", "TYPE IT AT CURSOR");
		}
		if (block instanceof BlockMixedFraction mixedFraction) {
			final ExpressionInputDiagnostic whole = diagnoseContainer(mixedFraction.getWholeContainer(), childBase,
					"INCOMPLETE MIXED FRACTION", "ENTER A WHOLE NUMBER", "TYPE IT AT CURSOR");
			if (whole != null)
				return whole;
			final int numeratorBase = childBase
					+ mixedFraction.getWholeContainer().computeCaretMaxBound();
			final ExpressionInputDiagnostic numerator = diagnoseContainer(
					mixedFraction.getNumeratorContainer(), numeratorBase,
					"INCOMPLETE MIXED FRACTION", "ENTER A NUMERATOR", "TYPE IT AT CURSOR");
			if (numerator != null)
				return numerator;
			return diagnoseContainer(mixedFraction.getDenominatorContainer(), numeratorBase
					+ mixedFraction.getNumeratorContainer().computeCaretMaxBound(),
					"INCOMPLETE MIXED FRACTION", "ENTER A DENOMINATOR", "TYPE IT AT CURSOR");
		}
		if (block instanceof BlockLogarithm logarithm) {
			final ExpressionInputDiagnostic base = diagnoseContainer(logarithm.getBaseContainer(), childBase,
					"INCOMPLETE LOGARITHM", "ENTER A LOGARITHM BASE", "TYPE IT AT CURSOR");
			if (base != null) {
				return base;
			}
			return diagnoseContainer(logarithm.getNumberContainer(),
					childBase + logarithm.getBaseContainer().computeCaretMaxBound(),
					"INCOMPLETE LOGARITHM", "ENTER A LOGARITHM VALUE", "TYPE IT AT CURSOR");
		}
		if (block instanceof BlockParenthesisAbstract parenthesis) {
			if (block instanceof BlockSine) {
				return diagnoseContainer(parenthesis.getNumberContainer(), childBase, "INCOMPLETE FUNCTION",
						"ENTER A SINE ARGUMENT", "TYPE IT AT CURSOR");
			}
			return diagnoseContainer(parenthesis.getNumberContainer(), childBase,
					"EMPTY PARENTHESES", "ENTER A VALUE INSIDE THE PARENTHESES", "TYPE IT AT CURSOR");
		}
		if (block instanceof BlockSquareRoot squareRoot) {
			return diagnoseContainer(squareRoot.getNumberContainer(), childBase,
					"INCOMPLETE ROOT", "ENTER A VALUE UNDER THE ROOT", "TYPE IT AT CURSOR");
		}
		return null;
	}

	private static ExpressionInputDiagnostic diagnosePower(final Block block, final int childBase) {
		final BlockContainer exponent;
		if (block instanceof BlockPower power) {
			exponent = power.getExponentContainer();
		} else if (block instanceof BlockPower2 power) {
			exponent = power.getExponentContainer();
		} else {
			return null;
		}
		return diagnoseContainer(exponent, childBase, "INCOMPLETE POWER", "ENTER AN EXPONENT",
				"TYPE IT AT CURSOR");
	}

	private static boolean isPower(final Block block) {
		return block instanceof BlockPower || block instanceof BlockPower2;
	}

	private static boolean isMinus(final char character) {
		return character == MathematicalSymbols.MINUS || character == MathematicalSymbols.SUBTRACTION;
	}

	private static boolean isBinaryOperator(final char character) {
		return character == MathematicalSymbols.SUM
				|| character == MathematicalSymbols.SUM_SUBTRACTION
				|| character == MathematicalSymbols.MULTIPLICATION
				|| character == MathematicalSymbols.DIVISION
				|| character == MathematicalSymbols.EQUATION
				|| character == MathematicalSymbols.LESS_THAN
				|| character == MathematicalSymbols.LESS_OR_EQUAL
				|| character == MathematicalSymbols.GREATER_THAN
				|| character == MathematicalSymbols.GREATER_OR_EQUAL;
	}

	private static boolean isPrefixSign(final char character) {
		return character == MathematicalSymbols.SUM
				|| character == MathematicalSymbols.SUM_SUBTRACTION;
	}

	private static String operatorLabel(final char operator) {
		return switch (operator) {
			case MathematicalSymbols.SUM_SUBTRACTION -> Character.toString(MathematicalSymbols.SUM_SUBTRACTION);
			case MathematicalSymbols.SUBTRACTION, MathematicalSymbols.MINUS -> "-";
			default -> Character.toString(operator);
		};
	}

	private static String incompleteTitle(final char operator) {
		return operator == MathematicalSymbols.EQUATION ? "INCOMPLETE EQUATION" : "INCOMPLETE EXPRESSION";
	}

	private static ExpressionInputDiagnostic missingBetween(final int position, final char leftOperator,
			final char rightOperator) {
		return next(position, "MISSING VALUE",
				"ENTER A VALUE BETWEEN " + operatorLabel(leftOperator) + " AND " + operatorLabel(rightOperator),
				"TYPE MISSING VALUE AT CURSOR");
	}

	private static ExpressionInputDiagnostic incompleteDecimal(final int position) {
		return previous(position, "INVALID NUMBER", "A DECIMAL POINT NEEDS A DIGIT",
				"TYPE A DIGIT OR DELETE THE DOT");
	}

	private static ExpressionInputDiagnostic previous(final int position, final String title, final String message,
			final String correction) {
		return new ExpressionInputDiagnostic(position, ExpressionInputDiagnostic.HighlightTarget.PREVIOUS_BLOCK, title,
				message, correction);
	}

	private static ExpressionInputDiagnostic next(final int position, final String title, final String message,
			final String correction) {
		return new ExpressionInputDiagnostic(position, ExpressionInputDiagnostic.HighlightTarget.NEXT_BLOCK, title,
				message, correction);
	}

	private static ExpressionInputDiagnostic insertion(final int position, final String title, final String message,
			final String correction) {
		return new ExpressionInputDiagnostic(position, ExpressionInputDiagnostic.HighlightTarget.INSERTION_POINT, title,
				message, correction);
	}
}
