package it.cavallium.warppi.math.rules.dsl.errorutils;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.io.File;
import java.util.List;

/**
 * Groups one or more errors from the same DSL source file.
 * <p>
 * Also stores the file's path and contents (for error reporting).
 */
public class FileErrors {
	private final File file;
	private final String source;
	private final List<DslError> errors;

	/**
	 * Constructs a <code>FileErrors</code> instance with the given file and error data.
	 *
	 * @param file The path of the DSL source file in which the errors occurred.
	 * @param source The entire contents of the DSL source file in which the errors occurred.
	 * @param errors The (non-empty) list of errors found in the DSL source file.
	 * @throws IllegalArgumentException If the list of errors is empty.
	 */
	public FileErrors(final File file, final String source, final List<DslError> errors) {
		if (errors.isEmpty()) {
			throw new IllegalArgumentException("The list of errors can't be empty");
		}
		this.file = file;
		this.source = source;
		this.errors = errors;
	}

	/**
	 * @return The path of the DSL source file in which the errors occurred.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return The entire contents of the DSL source file in which the errors occurred.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return The list of errors found in the DSL source file.
	 */
	public List<DslError> getErrors() {
		return errors;
	}
}
