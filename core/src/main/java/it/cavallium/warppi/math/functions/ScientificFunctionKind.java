package it.cavallium.warppi.math.functions;

/** Calculator functions exposed by the scientific keypad. */
public enum ScientificFunctionKind {
	ABSOLUTE("Abs", null),
	ROUND("Rnd", null),
	FACTORIZE("FACT", null),
	FACTORIAL(null, "!"),
	PERCENT(null, "%"),
	DEGREE_MINUTE_SECOND(null, "°′″");

	private final String prefix;
	private final String postfix;

	ScientificFunctionKind(final String prefix, final String postfix) {
		this.prefix = prefix;
		this.postfix = postfix;
	}

	public boolean isPostfix() {
		return postfix != null;
	}

	public String prefix() {
		if (prefix == null)
			throw new IllegalStateException(name() + " is a postfix function");
		return prefix;
	}

	public String postfix() {
		if (postfix == null)
			throw new IllegalStateException(name() + " is not a postfix function");
		return postfix;
	}
}
