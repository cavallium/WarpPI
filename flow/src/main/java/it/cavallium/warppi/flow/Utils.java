package it.cavallium.warppi.flow;

import java.lang.reflect.InvocationTargetException;

class Utils {

	/**
	 * Compatibility method for TeaVM
	 * @param t
	 * @param b
	 */
	public static void setThreadDaemon(Thread t, boolean b) {
		// Compatibility
		try {
			t.getClass().getMethod("setDaemon", Boolean.class).invoke(t, true);
		} catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

}
