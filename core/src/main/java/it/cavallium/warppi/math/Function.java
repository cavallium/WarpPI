package it.cavallium.warppi.math;

import java.io.Serializable;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface Function extends Serializable {

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
}
