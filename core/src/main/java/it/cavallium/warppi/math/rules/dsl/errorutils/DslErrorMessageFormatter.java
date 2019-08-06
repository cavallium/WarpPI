package it.cavallium.warppi.math.rules.dsl.errorutils;

import it.cavallium.warppi.math.rules.dsl.DslError;
import it.cavallium.warppi.math.rules.dsl.UndefinedSubFunction;
import it.cavallium.warppi.math.rules.dsl.frontend.*;

import java.util.stream.Collectors;

/**
 * Creates human-readable error messages from instances of {@link DslError}.
 */
public class DslErrorMessageFormatter implements DslError.Visitor<String> {
	/**
	 * Formats the given <code>DslError</code> as a human-readable message, according to its type.
	 *
	 * @param error The error to format.
	 * @return One or more lines of text which describe the error (without a trailing newline).
	 */
	public String format(final DslError error) {
		return error.accept(this);
	}

	@Override
	public String visit(final IncompleteNumberLiteral incompleteNumberLiteral) {
		return String.format(
				"Number has a decimal point but no digits following it: %s",
				incompleteNumberLiteral.getLiteral()
		);
	}

	@Override
	public String visit(final UndefinedSubFunction undefinedSubFunction) {
		return String.format(
				"Sub-function %s is used in a replacement pattern,\nbut not defined in the target pattern",
				undefinedSubFunction.getName()
		);
	}

	@Override
	public String visit(final UnexpectedCharacters unexpectedCharacters) {
		final String plural = unexpectedCharacters.getLength() > 1 ? "s" : "";
		return String.format("Unexpected character%s: %s", plural, unexpectedCharacters.getUnexpectedCharacters());
	}

	@Override
	public String visit(final UnexpectedToken unexpectedToken) {
		final String suggestions;
		if (unexpectedToken.getSuggested().isEmpty()) {
			suggestions = "";
		} else {
			suggestions = "\nSome suggestions are: " + unexpectedToken.getSuggested().stream()
					.map(DslErrorMessageFormatter::tokenTypeName)
					.collect(Collectors.joining(", "));
		}

		return String.format(
				"Unexpected %s%s",
				tokenTypeName(unexpectedToken.getUnexpected().type),
				suggestions
		);
	}

	@Override
	public String visit(final UnterminatedComment unterminatedComment) {
		return "Unterminated comment";
	}

	private static String tokenTypeName(final TokenType type) {
		switch (type) {
			case EOF:
				return "end of input";
			case COLON:
				return "\":\"";
			case ARROW:
				return "\"->\"";
			case COMMA:
				return "\",\"";
			case LEFT_PAREN:
				return "\"(\"";
			case RIGHT_PAREN:
				return "\")\"";
			case LEFT_BRACKET:
				return "\"[\"";
			case RIGHT_BRACKET:
				return "\"]\"";
			case EQUALS:
				return "\"=\"";
			case PLUS:
				return "\"+\"";
			case MINUS:
				return "\"-\"";
			case PLUS_MINUS:
				return "\"+-\"";
			case TIMES:
				return "\"*\"";
			case DIVIDE:
				return "\"/\"";
			case POWER:
				return "\"^\"";
			case REDUCTION:
			case EXPANSION:
			case CALCULATION:
			case EXISTENCE:
			case ARCCOS:
			case ARCSIN:
			case ARCTAN:
			case COS:
			case SIN:
			case TAN:
			case ROOT:
			case SQRT:
			case LOG:
			case UNDEFINED:
			case PI:
			case E:
				return '"' + type.name().toLowerCase() + '"';
			case NUMBER:
				return "number";
			case IDENTIFIER:
				return "identifier";
		}
		throw new RuntimeException("unknown token type");
	}
}
