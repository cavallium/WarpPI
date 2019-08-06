package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.dsl.frontend.IncompleteNumberLiteral;
import it.cavallium.warppi.math.rules.dsl.frontend.UnexpectedCharacters;
import it.cavallium.warppi.math.rules.dsl.frontend.UnexpectedToken;
import it.cavallium.warppi.math.rules.dsl.frontend.UnterminatedComment;

/**
 * Represents an error in DSL code.
 */
public interface DslError {
	/**
	 * @return The index at which the error starts in the source string.
	 */
	int getPosition();

	/**
	 * @return The length of the error in the source string.
	 */
	int getLength();

	/**
	 * Accepts a <code>DslError.Visitor</code> by calling the correct overload of <code>visit</code>.
	 *
	 * @param visitor The visitor to be accepted.
	 * @param <T> The return type of the <code>visit</code> method.
	 * @return The value returned by <code>visit</code>.
	 */
	<T> T accept(Visitor<T> visitor);

	/**
	 * Executes a different overload of a method for each <code>DslError</code> implementation.
	 *
	 * @param <T> The return type of all <code>visit</code> method overloads.
	 */
	interface Visitor<T> {
		T visit(IncompleteNumberLiteral incompleteNumberLiteral);
		T visit(UndefinedSubFunction undefinedSubFunction);
		T visit(UnexpectedCharacters unexpectedCharacters);
		T visit(UnexpectedToken unexpectedToken);
		T visit(UnterminatedComment unterminatedComment);
	}
}
