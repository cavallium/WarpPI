package it.cavallium.warppi.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

public class ScriptUtils {
	public static boolean instanceOf(final Object a, final Class<?> b) {
		return b.isInstance(a);
	}

	public static LinkedList<BigInteger> mcm(final LinkedList<BigInteger> factors1,
			final LinkedList<BigInteger> factors2) {
		final LinkedList<BigInteger> mcm = new LinkedList<>();
		final Iterator<BigInteger> i1 = factors1.iterator();
		while (i1.hasNext()) {
			final BigInteger int1 = i1.next();
			final Iterator<BigInteger> i2 = factors2.iterator();
			while (i2.hasNext()) {
				final BigInteger int2 = i2.next();
				if (int1.equals(int2)) {
					i1.remove();
					i2.remove();
					mcm.add(int1);
					break;
				}
			}
		}
		return mcm;
	}
}
