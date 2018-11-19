package it.cavallium.warppi.math;

import it.cavallium.warppi.math.functions.*;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.functions.equations.EquationsSystemPart;
import it.cavallium.warppi.math.functions.trigonometry.*;

/**
 * Executes a different overload of a method for each <code>Function</code> implementation.
 *
 * @param <T> The return type of all <code>visit</code> method overloads.
 */
public interface FunctionVisitor<T> {
	T visit(ArcCosine arcCosine);
	T visit(ArcSine arcSine);
	T visit(ArcTangent arcTangent);
	T visit(Cosine cosine);
	T visit(Division division);
	T visit(Equation equation);
	T visit(EquationsSystem equationsSystem);
	T visit(EquationsSystemPart equationsSystemPart);
	T visit(Expression expression);
	T visit(Joke joke);
	T visit(Logarithm logarithm);
	T visit(Multiplication multiplication);
	T visit(Negative negative);
	T visit(Number number);
	T visit(Power power);
	T visit(Root root);
	T visit(RootSquare rootSquare);
	T visit(Sine sine);
	T visit(Subtraction subtraction);
	T visit(SumSubtraction sumSubtraction);
	T visit(Sum sum);
	T visit(Tangent tangent);
	T visit(Undefined undefined);
	T visit(Variable variable);
}
