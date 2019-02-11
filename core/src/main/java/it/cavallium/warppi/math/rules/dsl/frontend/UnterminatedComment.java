package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.util.Objects;

/**
 * Occurs when DSL source code contains a multiline comment which is never terminated (closed).
 */
public class UnterminatedComment implements DslError {
	private final int position;

	public UnterminatedComment(final int position) {
		this.position = position;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public int getLength() {
		return 2; // Length of comment start marker: "/*"
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof UnterminatedComment)) {
			return false;
		}
		final UnterminatedComment other = (UnterminatedComment) o;
		return this.position == other.position;
	}

	@Override
	public int hashCode() {
		return Objects.hash(position);
	}
}
