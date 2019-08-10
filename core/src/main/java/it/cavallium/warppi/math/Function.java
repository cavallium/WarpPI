package it.cavallium.warppi.math;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.functions.*;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.functions.equations.EquationsSystemPart;
import it.cavallium.warppi.math.functions.trigonometry.*;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface Function {

	/**
	 * Returns this function and its children in a string form.
	 *
	 * @return This function and its children in a string form.
	 */
	@Override
	String toString();

	@Override
	boolean equals(Object o);

	/**
	 * Deep clone this function.
	 *
	 * @return A clone of this function.
	 */
	Function clone();

	/**
	 * Deep clone this function, also change mathContext.
	 *
	 * @param mathContext new mathContext
	 * @return A clone of this function.
	 */
	Function clone(MathContext newMathContext);

	/**
	 * Generic method to change a parameter in a known position.
	 *
	 * @param index
	 *            parameter index.
	 * @param var
	 *            parameter.
	 * @return A new instance of this function.
	 */
	Function setParameter(int index, Function var) throws IndexOutOfBoundsException;

	/**
	 * Generic method to retrieve a parameter in a known position.
	 *
	 * @param index
	 *            parameter index.
	 * @return The requested parameter.
	 */
	Function getParameter(int index) throws IndexOutOfBoundsException;

	/**
	 * Retrieve the current Math Context used by this function
	 *
	 * @return Calculator mathContext
	 */
	MathContext getMathContext();
	
	/**
	 * Simplify the current function or it's children using the specified
	 * <b>rule</b>
	 *
	 * @param rule
	 * @return A list of the resulting Functions if the rule is applicable and
	 *         something changed, <b>null</b> otherwise
	 * @throws Error
	 * @throws InterruptedException
	 */
	ObjectArrayList<Function> simplify(Rule rule) throws Error, InterruptedException;
	
	/**
	 *
	 * @param context
	 *            Mathematical Context
	 * @return An ArrayList of parsed Blocks
	 * @throws Error
	 */
	ObjectArrayList<Block> toBlock(MathContext context) throws Error;

	/**
	 * Accepts a {@code Function.Visitor<Argument, Result>} by calling the correct overload of <code>visit</code>.
	 *
	 * @param visitor    The visitor to be accepted.
	 * @param argument   An additional argument to be passed to <code>visit</code>.
	 * @param <Argument> The type of an additional argument to be passed to the <code>visit</code> method.
	 * @param <Result>   The return type of the <code>visit</code> method.
	 * @return The value returned by <code>visit</code>.
	 */
	<Argument, Result> Result accept(Visitor<Argument, Result> visitor, Argument argument);

	/**
	 * Executes a different overload of a method for each <code>Function</code> implementation.
	 *
	 * @param <Argument> The type of an additional argument which can be passed to all <code>visit</code> method overloads.
	 *                   If the argument is not required, this type parameter should be set to {@link Void}.
	 * @param <Result>   The return type of all <code>visit</code> method overloads.
	 */
	interface Visitor<Argument, Result> {
		Result visit(ArcCosine arcCosine, Argument argument);
		Result visit(ArcSine arcSine, Argument argument);
		Result visit(ArcTangent arcTangent, Argument argument);
		Result visit(Cosine cosine, Argument argument);
		Result visit(Division division, Argument argument);
		Result visit(Equation equation, Argument argument);
		Result visit(EquationsSystem equationsSystem, Argument argument);
		Result visit(EquationsSystemPart equationsSystemPart, Argument argument);
		Result visit(Expression expression, Argument argument);
		Result visit(Joke joke, Argument argument);
		Result visit(Logarithm logarithm, Argument argument);
		Result visit(Multiplication multiplication, Argument argument);
		Result visit(Negative negative, Argument argument);
		Result visit(Number number, Argument argument);
		Result visit(Power power, Argument argument);
		Result visit(Root root, Argument argument);
		Result visit(RootSquare rootSquare, Argument argument);
		Result visit(Sine sine, Argument argument);
		Result visit(Subtraction subtraction, Argument argument);
		Result visit(SumSubtraction sumSubtraction, Argument argument);
		Result visit(Sum sum, Argument argument);
		Result visit(Tangent tangent, Argument argument);
		Result visit(Undefined undefined, Argument argument);
		Result visit(Variable variable, Argument argument);
	}
}
