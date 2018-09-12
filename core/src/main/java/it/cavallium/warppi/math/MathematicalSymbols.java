package it.cavallium.warppi.math;

import static it.cavallium.warppi.util.Utils.concat;

import it.cavallium.warppi.util.Utils;

public class MathematicalSymbols {
	public static final char SUM = '+';
	public static final char SUM_SUBTRACTION = '±';
	public static final char SUBTRACTION = '−';
	public static final char MINUS = '-';
	public static final char MULTIPLICATION = '*';
	public static final char DIVISION = '/';
	public static final char NTH_ROOT = '√';
	public static final char SQUARE_ROOT = 'Ⓐ';
	public static final char PARENTHESIS_OPEN = '(';
	public static final char PARENTHESIS_CLOSE = ')';
	public static final char POWER = 'Ⓑ';
	public static final char EQUATION = '=';
	public static final char SYSTEM = '{';
	public static final char SINE = 'Ⓒ';
	public static final char COSINE = 'Ⓓ';
	public static final char TANGENT = 'Ⓔ';
	public static final char ARC_SINE = 'Ⓕ';
	public static final char ARC_COSINE = 'Ⓖ';
	public static final char ARC_TANGENT = 'Ⓗ';
	public static final char POWER_OF_TWO = 'Ⓘ';
	public static final char LOGARITHM = 'Ⓙ';
	public static final char UNDEFINED = '∅';
	public static final char PI = 'π';
	public static final char EULER_NUMBER = 'ｅ';
	public static final char X = 'ⓧ';
	public static final char Y = 'Ⓨ';

	public static final char[] functionsNSN = new char[] { NTH_ROOT, POWER };

	public static final char[] functionsSN = new char[] { SQUARE_ROOT, POWER_OF_TWO, MINUS, SINE, COSINE, TANGENT, ARC_SINE, ARC_COSINE, ARC_TANGENT, LOGARITHM };

	public static final char[] functions = concat(functionsNSN, functionsSN);

	private static final char[] signumsWithoutMultiplication = new char[] { SUM, SUM_SUBTRACTION, SUBTRACTION, DIVISION };
	private static final char[] signumsWithMultiplication = Utils.add(signumsWithoutMultiplication, MULTIPLICATION);

	public static final char[] functionsNSNAndSignums = concat(functionsNSN, signumsWithMultiplication);
	public static final char[] functionsAndSignums = concat(functions, signumsWithMultiplication);

	public static final char[] signums(boolean withMultiplication) {
		if (withMultiplication) {
			return signumsWithMultiplication;
		}
		return signumsWithoutMultiplication;
	}

	public static final char[] parentheses = new char[] { PARENTHESIS_OPEN, PARENTHESIS_CLOSE };

	public static final char[] variables = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', X, Y, 'Z', PI, EULER_NUMBER, UNDEFINED };

	public static final char[] genericSyntax = new char[] { SYSTEM, EQUATION };

	public static String getGraphicRepresentation(String string) {
		return string.replace("Ⓑ", "^").replace("Ⓒ", "SIN").replace("Ⓓ", "COS").replace("Ⓔ", "TAN").replace("Ⓕ", "ASIN").replace("Ⓖ", "ACOS").replace("Ⓗ", "ATAN");
	}

	public static final char[] numbers = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
}
