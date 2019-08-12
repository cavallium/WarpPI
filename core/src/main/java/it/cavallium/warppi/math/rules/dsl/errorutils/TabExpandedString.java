package it.cavallium.warppi.math.rules.dsl.errorutils;

import java.util.Arrays;

/**
 * Represents a line of text in which tabs have been expanded (replaced with spaces).
 * <p>
 * Each tab character is replaced with the number of spaces required to get to the next tab stop
 * (that is, the next column which is a multiple of the tab stop width).
 */
public class TabExpandedString {
	private final String expanded;
	private final int[] charWidths;

	/**
	 * Constructs a tab-expanded string with the given tab stop width.
	 *
	 * @param string The string to expand. Must not contain any line separator characters ('\r' or '\n').
	 * @param tabWidth The tab stop width.
	 * @throws IllegalArgumentException If <code>string</code> contains any line separator characters.
	 */
	public TabExpandedString(final String string, final int tabWidth) {
		final StringBuilder builder = new StringBuilder();
		charWidths = new int[string.length()];

		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);
			charWidths[i] = 1;

			switch (c) {
				case '\r':
				case '\n':
					throw new IllegalArgumentException("The string to expand is not a single line: " + string);
				case '\t':
					builder.append(' ');
					while (builder.length() % tabWidth != 0) {
						builder.append(' ');
						charWidths[i]++;
					}
					break;
				default:
					builder.append(c);
					break;
			}
		}

		expanded = builder.toString();
	}

	/**
	 * @return The tab-expanded string.
	 */
	public String getExpanded() {
		return expanded;
	}

	/**
	 * Computes the length of a substring of the original string after tab expansion.
	 *
	 * @param beginIndex The beginning index (inclusive) within the original string.
	 * @param endIndex The ending index (exclusive) within the original string.
	 * @return The length of the specified substring, after tabs have been expanded.
	 */
	public int substringLength(final int beginIndex, final int endIndex) {
		return Arrays.stream(charWidths, beginIndex, endIndex).sum();
	}
}
