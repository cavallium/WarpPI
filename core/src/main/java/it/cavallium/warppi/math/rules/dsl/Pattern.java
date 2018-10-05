package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;

import java.util.Map;
import java.util.Optional;

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
     * @param subFunctions A map of named sub-functions to be inserted into this pattern.
     * @return The resulting function.
     */
    Function replace(Map<String, Function> subFunctions);
}
