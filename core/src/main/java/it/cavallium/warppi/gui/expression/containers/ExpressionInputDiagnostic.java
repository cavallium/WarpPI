package it.cavallium.warppi.gui.expression.containers;

/**
 * A user-correctable problem found in the visual expression editor.
 *
 * @param caretPosition caret position where the next corrective edit belongs
 * @param highlightTarget expression element that should be underlined
 * @param title short category shown above the explanation
 * @param message concise explanation of the problem
 * @param correction concrete next action that can repair the input
 */
public record ExpressionInputDiagnostic(int caretPosition, HighlightTarget highlightTarget, String title,
		String message, String correction) {

	public enum HighlightTarget {
		INSERTION_POINT,
		PREVIOUS_BLOCK,
		NEXT_BLOCK
	}

	public ExpressionInputDiagnostic {
		if (caretPosition < 0) {
			throw new IllegalArgumentException("caretPosition must not be negative");
		}
		if (highlightTarget == null) {
			throw new IllegalArgumentException("highlightTarget must not be null");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
		if (correction == null || correction.isBlank()) {
			throw new IllegalArgumentException("correction must not be blank");
		}
	}
}
