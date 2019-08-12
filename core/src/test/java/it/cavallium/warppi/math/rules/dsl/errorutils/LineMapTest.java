package it.cavallium.warppi.math.rules.dsl.errorutils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineMapTest {
	@Test
	void emptyText() {
		String text = "";
		LineMap map = new LineMap(text);

		assertEquals(Collections.emptyList(), map.getSpannedLines(0, text.length()));
	}

	@Test
	void noLineSeparators() {
		String text = "single line";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, text)
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void trailingLf() {
		String text = "single line\n";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "single line")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void trailingCr() {
		String text = "single line\r";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "single line")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void trailingCrLf() {
		String text = "single line\r\n";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "single line")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void multipleNonEmptyLines() {
		String text = "line 1\nline 2\rline 3\r\nline 4";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Arrays.asList(
			new LineMap.Line(1, 0, "line 1"),
			new LineMap.Line(2, 7, "line 2"),
			new LineMap.Line(3, 14, "line 3"),
			new LineMap.Line(4, 22, "line 4")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void singleEmptyLine() {
		String text = "\n";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void multipleEmptyLines() {
		String text = "\r\n\n\r";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Arrays.asList(
			new LineMap.Line(1, 0, ""), // Terminated by \r\n
			new LineMap.Line(2, 2, ""), // Terminated by \n
			new LineMap.Line(3, 3, "") // Terminated by \r
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void mixedEmptyAndNonEmptyLines() {
		String text = "line 1\nline 2\r\r\nline 4\n\n";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Arrays.asList(
			new LineMap.Line(1, 0, "line 1"),
			new LineMap.Line(2, 7, "line 2"),
			new LineMap.Line(3, 14, ""),
			new LineMap.Line(4, 16, "line 4"),
			new LineMap.Line(5, 23, "")
		);
		assertEquals(expected, map.getSpannedLines(0, text.length()));
	}

	@Test
	void emptySubstrings() {
		String text = "single line\n";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "single line")
		);
		for (int start = 0; start <= text.length(); start++) {
			assertEquals(expected, map.getSpannedLines(start, 0));
		}
	}

	@Test
	void substringIsJustLineSeparator() {
		String separator = "\n";
		String text = "line 1" + separator + "line 2";
		LineMap map = new LineMap(text);

		List<LineMap.Line> expected = Collections.singletonList(
			new LineMap.Line(1, 0, "line 1")
		);
		assertEquals(expected, map.getSpannedLines(text.indexOf(separator), separator.length()));
	}
}