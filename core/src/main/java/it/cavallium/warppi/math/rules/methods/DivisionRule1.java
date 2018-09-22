package it.cavallium.warppi.math.rules.methods;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Division method<br>
 * <b>Example: (XY)/(YZ) = Y/Y * X/Z</b>
 *
 * @author Andrea Cavalli
 *
 */
public class DivisionRule1 {

	public static boolean compare(final Division f) throws InterruptedException {
		return false;//TODO:    return f.getParameter1().isSimplified() && f.getParameter2().isSimplified() && (f.getParameter1() instanceof Multiplication || f.getParameter2() instanceof Multiplication) && getFirstWorkingDivisionCouple(getDivisionElements(f)) != null;
	}

	public static ObjectArrayList<Function> execute(final Division f) throws Error, InterruptedException {
		final MathContext root = f.getMathContext();
		Function result;
		final ObjectArrayList<Function>[] elements = DivisionRule1.getDivisionElements(f);
		final int[] workingElementCouple = DivisionRule1.getFirstWorkingDivisionCouple(elements);
		final Function elem1 = elements[0].get(workingElementCouple[0]);
		final Function elem2 = elements[1].get(workingElementCouple[1]);

		/*final int size = elements.size();
		Function prec = new Multiplication(root, elem1, elem2);
		for (int i = size - 1; i >= 0; i--) {
			if (i != workingElementCouple[0] & i != workingElementCouple[1]) {
				final Function a = prec;
				final Function b = elements.get(i);
				prec = new Multiplication(root, a, b);
			}
		}*/

		final int[] size = new int[] { elements[0].size(), elements[1].size() };
		final Function separatedDivision = new Division(root, elem1, elem2);

		final Function[] resultDivisionArray = new Function[2];
		Function prec;
		for (int part = 0; part < 2; part++) {
			prec = null;
			for (int i = size[part] - 1; i >= 0; i--)
				if (i != workingElementCouple[part])
					if (prec == null)
						prec = elements[part].get(i);
					else {
						final Function a = elements[part].get(i);
						final Function b = prec;
						prec = new Multiplication(root, a, b);
					}
			if (prec == null)
				prec = new Number(root, 1);
			resultDivisionArray[part] = prec;
		}

		result = new Multiplication(root, separatedDivision, new Division(root, resultDivisionArray[0], resultDivisionArray[1]));

		final ObjectArrayList<Function> results = new ObjectArrayList<>();
		results.add(result);
		return results;
	}

	@SuppressWarnings("unchecked")
	private static ObjectArrayList<Function>[] getDivisionElements(final Division division)
			throws InterruptedException {
		final ObjectArrayList<Function> elementsNumerator = new ObjectArrayList<>();
		Function numMult = division.getParameter1();
		while (numMult instanceof Multiplication) {
			if (Thread.interrupted())
				throw new InterruptedException();
			elementsNumerator.add(((Multiplication) numMult).getParameter1());
			numMult = ((Multiplication) numMult).getParameter2();
		}
		elementsNumerator.add(numMult);

		final ObjectArrayList<Function> elementsDenominator = new ObjectArrayList<>();
		Function denomMult = division.getParameter2();
		while (denomMult instanceof Multiplication) {
			if (Thread.interrupted())
				throw new InterruptedException();
			elementsDenominator.add(((Multiplication) denomMult).getParameter1());
			denomMult = ((Multiplication) denomMult).getParameter2();
		}
		elementsDenominator.add(denomMult);

		return new ObjectArrayList[] { elementsNumerator, elementsDenominator };
	}

	private static int[] getFirstWorkingDivisionCouple(final ObjectArrayList<Function>[] elements)
			throws InterruptedException {
		return null;
		//TODO:
//		final int[] size = new int[] { elements[0].size(), elements[1].size() };
//		Function a;
//		Function b;
//		if (elements[0].size() + elements[1].size() <= 2) {
//			return null;
//		}
//		for (int i = 0; i < size[0]; i++) {
//			a = elements[0].get(i);
//			for (int j = 0; j < size[1]; j++) {
//				if (Thread.interrupted()) throw new InterruptedException();
//				b = elements[1].get(j);
//				Function testFunc;
//				testFunc = new Division(a.getMathContext(), a, b);
//				if (!testFunc.isSimplified()) {
//					return new int[] { i, j };
//				}
//			}
//		}
//		return null;
	}

}
