package org.warp.picalculator.deps;

import java.io.PrintWriter;

public class DJDTCompiler {

	public static boolean compile(String[] strings, PrintWriter printWriter, PrintWriter printWriter2) {
		return org.eclipse.jdt.internal.compiler.batch.Main.compile(strings, printWriter, printWriter2, null);
	}
}
