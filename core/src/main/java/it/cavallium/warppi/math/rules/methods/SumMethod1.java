package it.cavallium.warppi.math.rules.methods;

import java.math.BigDecimal;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Sum method<br>
 * <b>13+sqrt(2)+5X+1 = 14+sqrt(2)+5X</b>
 *
 * @author Andrea Cavalli
 *
 */
public class SumMethod1 {

	public static boolean compare(final Function f) throws InterruptedException {
		return false;
		//TODO:
//		final MathContext root = f.getMathContext();
//		return (f instanceof Sum || f instanceof Subtraction) && ((FunctionOperator) f).getParameter1().isSimplified() && ((FunctionOperator) f).getParameter2().isSimplified() && !(((FunctionOperator) f).getParameter1() instanceof Number && ((FunctionOperator) f).getParameter2() instanceof Number) && getFirstWorkingSumCouple(root, getSumElements(f)) != null;
	}

	public static ObjectArrayList<Function> execute(final Function f) throws Error, InterruptedException {
		Function result;
		final MathContext root = f.getMathContext();
		final ObjectArrayList<Function> elements = SumMethod1.getSumElements(f);
		final int[] workingElementCouple = SumMethod1.getFirstWorkingSumCouple(root, elements);
		final Function elem1 = elements.get(workingElementCouple[0]);
		final Function elem2 = elements.get(workingElementCouple[1]);

		final int size = elements.size();
		Function prec = new Sum(root, elem1, elem2);
		for (int i = size - 1; i >= 0; i--) {
			if (i != workingElementCouple[0] & i != workingElementCouple[1]) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				final Function a = prec;
				final Function b = elements.get(i);
				if (b instanceof Negative) {
					prec = new Subtraction(root, a, ((Negative) b).getParameter());
					((FunctionOperator) prec).getParameter2();
				} else if (b instanceof Number && ((Number) b).getTerm().compareTo(BigDecimal.ZERO) < 0) {
					prec = new Subtraction(root, a, ((Number) b).multiply(new Number(root, -1)));
					((FunctionOperator) prec).getParameter2();
				} else {
					prec = new Sum(root, a, b);
				}
			}
		}

		result = prec;

		final ObjectArrayList<Function> results = new ObjectArrayList<>();
		results.add(result);
		return results;
	}

	private static ObjectArrayList<Function> getSumElements(Function sum) throws InterruptedException {
		final MathContext root = sum.getMathContext();
		final ObjectArrayList<Function> elements = new ObjectArrayList<>();
		while (sum instanceof Sum || sum instanceof Subtraction) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (sum instanceof Sum) {
				elements.add(((FunctionOperator) sum).getParameter2());
			} else {
				elements.add(new Negative(root, ((FunctionOperator) sum).getParameter2()));
			}
			sum = ((FunctionOperator) sum).getParameter1();
		}
		elements.add(sum);
		return elements;
	}

	private static int[] getFirstWorkingSumCouple(final MathContext root, final ObjectArrayList<Function> elements)
			throws InterruptedException {
		return null;
//		final int size = elements.size();
//		Function a;
//		Function b;
//		if (elements.size() == 2) {
//			return null;
//		}
//		for (int i = 0; i < size; i++) {
//			a = elements.get(i);
//			for (int j = 0; j < size; j++) {
//				if (Thread.interrupted()) throw new InterruptedException();
//				b = elements.get(j);
//				if (i != j) {
//					Function testFunc;
//					if (b instanceof Negative) {
//						testFunc = new Subtraction(root, a, ((Negative) b).getParameter());
//					} else if (b instanceof Number && ((Number) b).getTerm().compareTo(BigDecimal.ZERO) < 0) {
//						testFunc = new Subtraction(root, a, ((Number) b).multiply(new Number(root, -1)));
//					} else if (a instanceof Negative) {
//						testFunc = new Subtraction(root, b, ((Negative) a).getParameter());
//					} else if (a instanceof Number && ((Number) a).getTerm().compareTo(BigDecimal.ZERO) < 0) {
//						testFunc = new Subtraction(root, b, ((Number) a).multiply(new Number(root, -1)));
//					} else {
//						testFunc = new Sum(root, a, b);
//					}
//					if (!testFunc.isSimplified()) {
//						return new int[] { i, j };
//					}
//				}
//			}
//		}
//		return null;
	}

}
