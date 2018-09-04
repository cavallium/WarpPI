package it.cavallium.warppi.math.rules;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Rule interface
 * 
 * @author Andrea Cavalli
 *
 */
public interface Rule {
	/**
	 * Get rule name
	 * 
	 * @return
	 */
	public default String getRuleName() {
		return "UnnamedRule1";
	}

	/**
	 * Get rule type
	 * 
	 * @return
	 */
	public RuleType getRuleType();

	/**
	 * 
	 * @param func
	 * @return
	 *         <ul>
	 *         <li><code>null</code> if it's not executable on the function
	 *         <b>func</b></li>
	 *         <li>An <code>ObjectArrayList&lt;Function&gt;</code> if it did
	 *         something</li>
	 *         </ul>
	 * @throws Error
	 */
	public ObjectArrayList<Function> execute(Function func) throws Error, InterruptedException;
}