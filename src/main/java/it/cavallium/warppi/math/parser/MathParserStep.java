package it.cavallium.warppi.math.parser;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.IntegerObj;
import it.cavallium.warppi.math.Function;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Join number and variables together ([2][4][x] => [[24]*[x]])
 * 
 * @author Andrea Cavalli
 *
 */
public interface MathParserStep {
	/**
	 * 
	 * @param f
	 * @param curIndex
	 * @param process
	 * @return true if something changed
	 */
	public boolean eval(IntegerObj curIndex, Function lastFunction, Function currentFunction,
			ObjectArrayList<Function> functionsfunctionsList) throws Error;

	public boolean requiresReversedIteration();

	public String getStepName();
}