package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates a specific symbolic constant.
 */
public class ConstantPattern extends VisitorPattern {
	private final char symbol;

	public ConstantPattern(final char symbol) {
		this.symbol = symbol;
	}

	@Override
	public Boolean visit(final Variable variable, final Map<String, Function> subFunctions) {
		return variable.getType().equals(Variable.V_TYPE.CONSTANT)
			&& variable.getChar() == symbol;
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Variable(mathContext, symbol, Variable.V_TYPE.CONSTANT);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof ConstantPattern)) {
			return false;
		}
		final ConstantPattern other = (ConstantPattern) o;
		return symbol == other.symbol;
	}

	@Override
	public int hashCode() {
		return Objects.hash(symbol);
	}
}
