package it.cavallium.warppi.math.rules.dsl.errorutils;

import it.cavallium.warppi.math.rules.dsl.DslError;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Formats DSL errors from (potentially) multiple files for display to the user.
 */
public class FilesErrorsFormatter {
	private static final int INDENT = 2;
	private static final int TAB_WIDTH = 4;

	private final DslErrorMessageFormatter messageFormatter = new DslErrorMessageFormatter();

	/**
	 * Formats all errors in the given list.
	 *
	 * @param filesErrors The list of errors to format.
	 * @return A human-readable textual representation of all errors (with a trailing newline).
	 */
	public String format(final List<FileErrors> filesErrors) {
		return filesErrors.stream()
				.sorted(Comparator.comparing(FileErrors::getFilePath))
				.flatMap(this::formatFileErrors)
				.collect(Collectors.joining(System.lineSeparator()));
	}

	private Stream<String> formatFileErrors(final FileErrors fileErrors) {
		final LineMap lines = new LineMap(fileErrors.getSource());
		return fileErrors.getErrors().stream()
				.sorted(Comparator.comparing(DslError::getPosition).thenComparing(DslError::getLength))
				.map(error -> formatError(fileErrors.getFilePath(), lines, error));
	}

	private String formatError(final String filePath, final LineMap lines, final DslError error) {
		final StringBuilder builder = new StringBuilder();

		final List<LineMap.Line> spannedLines = lines.getSpannedLines(error.getPosition(), error.getLength());
		final LineMap.Line firstLine = spannedLines.get(0);

		final int positionInFirstLine = error.getPosition() - firstLine.getStartPosition();
		final TabExpandedString expandedFirstLine = new TabExpandedString(firstLine.getText(), TAB_WIDTH);
		// When computing the column number, each tab character is counted as the number of spaces it expands to
		final int column = 1 + expandedFirstLine.substringLength(0, positionInFirstLine);

		builder.append(filePath).append(":")
				.append(firstLine.getNumber()).append(":")
				.append(column).append(":")
				.append(System.lineSeparator());

		final int lastLineNum = spannedLines.get(spannedLines.size() - 1).getNumber();
		final int numberWidth = String.valueOf(lastLineNum).length();
		final int padding = INDENT + numberWidth;

		// Preceding line with just separator
		builder.append(StringUtils.repeat(' ', padding))
				.append(" |")
				.append(System.lineSeparator());

		for (final LineMap.Line line : spannedLines) {
			// Error text line
			final TabExpandedString expanded = new TabExpandedString(line.getText(), TAB_WIDTH);
			builder.append(StringUtils.leftPad(String.valueOf(line.getNumber()), padding))
					.append(" | ")
					.append(expanded.getExpanded())
					.append(System.lineSeparator());

			// Error underlining line
			builder.append(StringUtils.repeat(' ', padding)).append(" | ");
			underline(builder, line, expanded, error);
			builder.append(System.lineSeparator());
		}

		builder.append(messageFormatter.format(error)).append(System.lineSeparator());

		return builder.toString();
	}

	private void underline(
			final StringBuilder builder,
			final LineMap.Line line,
			final TabExpandedString expanded,
			final DslError error
	) {
		final int errorStartInLine = Math.max(line.getStartPosition(), error.getPosition());
		final int charsBeforeError = errorStartInLine - line.getStartPosition();
		final int spacesBeforeError = expanded.substringLength(0, charsBeforeError);
		builder.append(StringUtils.repeat(' ', spacesBeforeError));

		final int underlineLength;
		if (error.getLength() == 0) {
			underlineLength = 1; // Special case for "unexpected EOF" error
		} else {
			final int errorEnd = error.getPosition() + error.getLength();
			final int errorLengthInLine = Math.min(line.getText().length() - charsBeforeError, errorEnd - errorStartInLine);
			underlineLength = expanded.substringLength(charsBeforeError, charsBeforeError + errorLengthInLine);
		}
		builder.append(StringUtils.repeat('^', underlineLength));
	}
}
