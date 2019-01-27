package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Recognizes and generates functions of some specific shape.
 */
public interface Pattern {
	/**
	 * Tries to match this pattern against a function and capture sub-functions.
	 *
	 * @param function The function to test the pattern against.
	 * @return The captured sub-functions, or an empty <code>Optional</code> if
	 *         the pattern doesn't match.
	 */
	Optional<Map<String, Function>> match(Function function);

	/**
	 * Creates a new function by filling in sub-functions within this pattern.
	 *
	 * @param mathContext The <code>MathContext</code> used to construct <code>Function</code>s.
	 * @param subFunctions A map of named sub-functions to be inserted into this pattern.
	 * @return The resulting function.
	 */
	Function replace(MathContext mathContext, Map<String, Function> subFunctions);

	/**
	 * @return The (possibly empty) <code>Set</code> of all sub-function patterns
	 *         found within this pattern and its children.
	 */
	Set<SubFunctionPattern> getSubFunctions();
}
