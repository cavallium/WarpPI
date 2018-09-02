package it.cavallium.warppi.math.rules;

public enum RuleType {
	/**
	 * A rule that tries to factorize and group a polynomial expression into a
	 * shorter expression
	 */
	REDUCTION,
	/**
	 * A rule that tries to transform an expression to a simple polynomial
	 * expression
	 */
	EXPANSION,
	/**
	 * Calculation
	 */
	CALCULATION,
	/**
	 * Existence
	 */
	EXISTENCE
}
