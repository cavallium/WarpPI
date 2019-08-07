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
	 * Accepts a <code>Function.Visitor</code> by calling the correct overload of <code>visit</code>.
	 *
	 * @param visitor The visitor to be accepted.
	 * @param <T> The return type of the <code>visit</code> method.
	 * @return The value returned by <code>visit</code>.
	 */
	<T> T accept(Visitor<T> visitor);

	/**
	 * Executes a different overload of a method for each <code>Function</code> implementation.
	 *
	 * @param <T> The return type of all <code>visit</code> method overloads.
	 */
	interface Visitor<T> {
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
}
