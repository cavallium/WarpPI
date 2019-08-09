package it.cavallium.warppi.math.rules.dsl.errorutils;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when one or more DSL source files contain errors.
 */
public class DslFilesException extends Exception {
	private final List<FileErrors> filesErrors = new ArrayList<>();

	/**
	 * Registers errors which have been found in the specified DSL source file.
	 *
	 * @param filePath The path of the DSL source file in which the errors occurred.
	 * @param source   The entire contents of the DSL source file in which the errors occurred.
	 * @param errors   The (non-empty) list of errors found in the DSL source file.
	 * @throws IllegalArgumentException If the list of errors is empty.
	 */
	public void addFileErrors(final String filePath, final String source, final List<DslError> errors) {
		filesErrors.add(new FileErrors(filePath, source, errors));
	}

	/**
	 * Checks if any errors have been registered.
	 * <p>
	 * Instances of this class should only be thrown as exceptions if they actually contain errors.
	 *
	 * @return <code>true</code> if at least one error has been added, otherwise <code>false</code>.
	 */
	public boolean hasErrors() {
		return !filesErrors.isEmpty();
	}

	/**
	 * Formats all errors using a {@link FilesErrorsFormatter}.
	 *
	 * @return A formatted representation of all errors for display to the user.
	 * @see FilesErrorsFormatter#format(List)
	 */
	public String format() {
		return new FilesErrorsFormatter().format(filesErrors);
	}
}
