package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.*;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.functions.equations.EquationsSystemPart;
import it.cavallium.warppi.math.functions.trigonometry.*;

import java.util.Map;

/**
 * A <code>Pattern</code> which implements <code>match</code> as a visitor.
 */
public abstract class VisitorPattern implements Pattern, Function.Visitor<Map<String, Function>, Boolean> {
	@Override
	public boolean match(Function function, Map<String, Function> subFunctions) {
		return function.accept(this, subFunctions);
	}

	@Override
	public Boolean visit(final ArcCosine arcCosine, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final ArcSine arcSine, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final ArcTangent arcTangent, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Cosine cosine, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Division division, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Equation equation, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final EquationsSystem equationsSystem, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final EquationsSystemPart equationsSystemPart, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Expression expression, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Joke joke, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Logarithm logarithm, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Multiplication multiplication, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Negative negative, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Number number, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Power power, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Root root, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final RootSquare rootSquare, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Sine sine, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Subtraction subtraction, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final SumSubtraction sumSubtraction, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Sum sum, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Tangent tangent, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Undefined undefined, final Map<String, Function> subFunctions) {
		return false;
	}

	@Override
	public Boolean visit(final Variable variable, final Map<String, Function> subFunctions) {
		return false;
	}
}
