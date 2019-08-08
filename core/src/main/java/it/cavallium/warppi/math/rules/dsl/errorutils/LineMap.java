package it.cavallium.warppi.math.rules.dsl.errorutils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Splits a string into lines and associates positions within the string to the lines they belong to.
 * <p>
 * For each line, the number (starting from 1), start position and content are stored.
 * <p>
 * A line can end at the end of the string, or with a line terminator ("\r", "\n" or "\r\n").
 * The terminator defines the end of a line, but not necessarily the beginning of a new one: it's considered to be part
 * of the line (however, for convenience, it's not included in the content), and a terminator at the end of the string
 * doesn't start a new empty line.
 * For example, the string <code>"abc\n\n"</code> contains two lines:
 * <ul>
 * <li> line 1 starts at position 0, and its content is <code>"abc"</code>;
 * <li> line 2 starts at position 4 (the index of the second '\n'), and its content is <code>""</code>  (the empty string).
 * </ul>
 * As a consequence of these criteria, an empty string has no lines.
 */
public class LineMap {
	private final String text;
	private final NavigableMap<Integer, LineInfo> lines;

	/**
	 * Constructs a <code>LineMap</code> for the given string.
	 *
	 * @param text The string to split into lines.
	 */
	public LineMap(final String text) {
		this.text = text;
		this.lines = splitLines(text);
	}

	/**
	 * Gets all lines which the specified substring spans.
	 * <p>
	 * A substring spans a line if it contains at least one of the characters which belong to the line,
	 * including the terminator ("\r", "\n" or "\r\n"), within the original string.
	 * However, as a special case, an empty substring (<code>length == 0</code>) still spans the line corresponding to
	 * its <code>startPosition</code>, even though it doesn't contain any characters.
	 * Therefore, any substring spans at least one line, unless there are no lines at all (because the original string
	 * is empty).
	 *
	 * @param startPosition The index at which the substring starts within the original string.
	 * @param length The length of the substring within the original string.
	 * @return The (potentially empty) list of spanned lines (each one without the terminator characters).
	 * @throws StringIndexOutOfBoundsException If the specified substring isn't valid, because:
	 *                                         <ul>
	 *                                         <li> <code>startPosition</code> is negative, or
	 *                                         <li> <code>startPosition</code> is larger than the length of the original string, or
	 *                                         <li> <code>length</code> is negative, or
	 *                                         <li> there are less than <code>length</code> characters from <code>startPosition</code>
	 *                                              to the end of the original string.
	 *                                         </ul>
	 */
	public List<Line> getSpannedLines(final int startPosition, final int length) {
		if (startPosition < 0 || startPosition > text.length()) {
			throw new StringIndexOutOfBoundsException("Substring start position out of range: " + startPosition);
		}
		int endPosition = startPosition + length;
		if (endPosition < startPosition || endPosition > text.length()) {
			throw new StringIndexOutOfBoundsException("Substring length out of range: " + length);
		}

		if (lines.isEmpty()) {
			return Collections.emptyList();
		}

		final Map.Entry<Integer, LineInfo> firstSpannedLine = lines.floorEntry(startPosition);
		if (length == 0) {
			// For empty substrings, firstSpannedLine.getKey() may be equal to endPosition.
			// In this case, the submap would be empty (because the upper bound is exclusive),
			// so the single spanned line has to be returned manually.
			return Collections.singletonList(lineFromMapEntry(firstSpannedLine));
		}
		final SortedMap<Integer, LineInfo> spannedLines = lines.subMap(firstSpannedLine.getKey(), endPosition);
		return spannedLines.entrySet().stream()
				.map(this::lineFromMapEntry)
				.collect(Collectors.toList());
	}

	private static NavigableMap<Integer, LineInfo> splitLines(final String string) {
		final TreeMap<Integer, LineInfo> lines = new TreeMap<>();

		int lineNum = 1;
		int lineStart = 0;
		int pos = 0;
		while (pos < string.length()) {
			final char cur = string.charAt(pos);
			int nextPos = pos + 1;
			if (nextPos < string.length() && cur == '\r' && string.charAt(nextPos) == '\n') {
				nextPos++; // Skip \n after \r because \r\n is a single line separator
			}
			if (cur == '\r' || cur == '\n') {
				lines.put(lineStart, new LineInfo(lineNum, pos));
				lineNum++;
				lineStart = nextPos;
			}
			pos = nextPos;
		}
		// If the last line has no trailing separator, the loop won't add it to the map
		if (lineStart < string.length()) {
			lines.put(lineStart, new LineInfo(lineNum, string.length()));
		}

		return lines;
	}

	private Line lineFromMapEntry(final Map.Entry<Integer, LineInfo> entry) {
		final int start = entry.getKey();
		final LineInfo lineInfo = entry.getValue();
		return new Line(
				lineInfo.number,
				start,
				text.substring(start, lineInfo.end)
		);
	}

	private static class LineInfo {
		public final int number;
		public final int end;

		LineInfo(final int number, final int end) {
			this.number = number;
			this.end = end;
		}
	}

	/**
	 * Represents a line of text within a string.
	 */
	public static class Line {
		private final int number;
		private final int startPosition;
		private final String text;

		Line(final int number, final int startPosition, final String text) {
			this.number = number;
			this.startPosition = startPosition;
			this.text = text;
		}

		/**
		 * @return The line number (starting from 1).
		 */
		public int getNumber() {
			return number;
		}

		/**
		 * @return The index at which this line starts within the original string.
		 * 		   If the line is empty, this is the index of the terminator characters ("\r", "\n" or "\r\n").
		 */
		public int getStartPosition() {
			return startPosition;
		}

		/**
		 * @return The contents of this line, <em>without</em> the terminator characters ("\r", "\n" or "\r\n").
		 */
		public String getText() {
			return text;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Line line = (Line) o;
			return number == line.number &&
				startPosition == line.startPosition &&
				Objects.equals(text, line.text);
		}

		@Override
		public int hashCode() {
			return Objects.hash(number, startPosition, text);
		}

		@Override
		public String toString() {
			return "Line{" +
				"number=" + number +
				", startPosition=" + startPosition +
				", text='" + text + '\'' +
				'}';
		}
	}
}
