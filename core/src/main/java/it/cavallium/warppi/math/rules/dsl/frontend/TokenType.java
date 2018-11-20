package it.cavallium.warppi.math.rules.dsl.frontend;

public enum TokenType {
	EOF,
	// Separators and grouping
	COLON, ARROW, COMMA, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
	// Operators
	EQUALS, PLUS, MINUS, PLUS_MINUS, TIMES, DIVIDE, POWER,
	// Rule types
	REDUCTION, EXPANSION, CALCULATION, EXISTENCE,
	// Functions
	ARCCOS, ARCSIN, ARCTAN, COS, SIN, TAN, ROOT, SQRT, LOG,
	// Literals
	UNDEFINED, PI, E, NUMBER, IDENTIFIER,
}
