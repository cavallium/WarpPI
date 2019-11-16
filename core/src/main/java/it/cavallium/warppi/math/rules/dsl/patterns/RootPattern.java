package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Root;
import it.cavallium.warppi.math.functions.RootSquare;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates a root of degree and radicand patterns.
 * <p>
 * Also matches and generates functions of type <code>RootSquare</code>.
 */
public class RootPattern extends VisitorPattern {
	private final Pattern degree;
	private final Pattern radicand;

	public RootPattern(final Pattern degree, final Pattern radicand) {
		this.degree = degree;
		this.radicand = radicand;
	}

	@Override
	public Boolean visit(final Root root, final Map<String, Function> subFunctions) {
		return degree.match(root.getParameter1(), subFunctions)
			&& radicand.match(root.getParameter2(), subFunctions);
	}

	@Override
	public Boolean visit(final RootSquare rootSquare, final Map<String, Function> subFunctions) {
		return degree.match(rootSquare.getDegree(), subFunctions)
			&& radicand.match(rootSquare.getParameter(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		final Function newDegree = degree.replace(mathContext, subFunctions);
		final Function newRadicand = radicand.replace(mathContext, subFunctions);

		if (newDegree instanceof Number
			&& ((Number) newDegree).getTerm().compareTo(new BigDecimal(2)) == 0) {
			return new RootSquare(mathContext, newRadicand);
		} else {
			return new Root(mathContext, newDegree, newRadicand);
		}
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(degree, radicand)
			.flatMap(Pattern::getSubFunctions);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof RootPattern)) {
			return false;
		}
		final RootPattern other = (RootPattern) o;
		return degree.equals(other.degree) && radicand.equals(other.radicand);
	}

	@Override
	public int hashCode() {
		return Objects.hash(degree, radicand);
	}
}
