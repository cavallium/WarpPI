package it.cavallium.warppi.math;

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

	public static final char[] functionsNSN = new char[] { MathematicalSymbols.NTH_ROOT, MathematicalSymbols.POWER };

	public static final char[] functionsSN = new char[] { MathematicalSymbols.SQUARE_ROOT, MathematicalSymbols.POWER_OF_TWO, MathematicalSymbols.MINUS, MathematicalSymbols.SINE, MathematicalSymbols.COSINE, MathematicalSymbols.TANGENT, MathematicalSymbols.ARC_SINE, MathematicalSymbols.ARC_COSINE, MathematicalSymbols.ARC_TANGENT, MathematicalSymbols.LOGARITHM };

	public static final char[] functions = Utils.concat(MathematicalSymbols.functionsNSN, MathematicalSymbols.functionsSN);

	private static final char[] signumsWithoutMultiplication = new char[] { MathematicalSymbols.SUM, MathematicalSymbols.SUM_SUBTRACTION, MathematicalSymbols.SUBTRACTION, MathematicalSymbols.DIVISION };
	private static final char[] signumsWithMultiplication = Utils.add(MathematicalSymbols.signumsWithoutMultiplication, MathematicalSymbols.MULTIPLICATION);

	public static final char[] functionsNSNAndSignums = Utils.concat(MathematicalSymbols.functionsNSN, MathematicalSymbols.signumsWithMultiplication);
	public static final char[] functionsAndSignums = Utils.concat(MathematicalSymbols.functions, MathematicalSymbols.signumsWithMultiplication);

	public static final char[] signums(final boolean withMultiplication) {
		if (withMultiplication)
			return MathematicalSymbols.signumsWithMultiplication;
		return MathematicalSymbols.signumsWithoutMultiplication;
	}

	public static final char[] parentheses = new char[] { MathematicalSymbols.PARENTHESIS_OPEN, MathematicalSymbols.PARENTHESIS_CLOSE };

	public static final char[] variables = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', MathematicalSymbols.X, MathematicalSymbols.Y, 'Z', MathematicalSymbols.PI, MathematicalSymbols.EULER_NUMBER, MathematicalSymbols.UNDEFINED };

	public static final char[] genericSyntax = new char[] { MathematicalSymbols.SYSTEM, MathematicalSymbols.EQUATION };

	public static String getGraphicRepresentation(final String string) {
		return string.replace("Ⓑ", "^").replace("Ⓒ", "SIN").replace("Ⓓ", "COS").replace("Ⓔ", "TAN").replace("Ⓕ", "ASIN").replace("Ⓖ", "ACOS").replace("Ⓗ", "ATAN");
	}

	public static final char[] numbers = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
}
