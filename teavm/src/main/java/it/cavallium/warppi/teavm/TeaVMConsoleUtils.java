package it.cavallium.warppi.teavm;

import java.io.PrintStream;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.math.MathematicalSymbols;

public class TeaVMConsoleUtils implements it.cavallium.warppi.deps.Platform.ConsoleUtils {

	private AdvancedOutputStream os;

	public TeaVMConsoleUtils() {
		os = new AdvancedOutputStream() {
			private void print(PrintStream stream, String str) {
				stream.print(fixString(str));
			}

			private void println(PrintStream stream, String str) {
				stream.println(fixString(str));
			}

			private void println(PrintStream stream) {
				stream.println();
			}

			private String fixString(String str) {

				return str.replace("" + MathematicalSymbols.NTH_ROOT, "root").replace("" + MathematicalSymbols.SQUARE_ROOT, "sqrt").replace("" + MathematicalSymbols.POWER, "powerOf").replace("" + MathematicalSymbols.POWER_OF_TWO, "powerOfTwo").replace("" + MathematicalSymbols.SINE, "sine").replace("" + MathematicalSymbols.COSINE, "cosine").replace("" + MathematicalSymbols.TANGENT, "tangent").replace("" + MathematicalSymbols.ARC_SINE, "asin").replace("" + MathematicalSymbols.ARC_COSINE, "acos").replace("" + MathematicalSymbols.ARC_TANGENT, "atan").replace("" + MathematicalSymbols.UNDEFINED, "undefined").replace("" + MathematicalSymbols.PI, "PI").replace("" + MathematicalSymbols.EULER_NUMBER, "EULER_NUMBER").replace("" + MathematicalSymbols.X, "X").replace("" + MathematicalSymbols.Y, "Y");
			}

			public void println(Object str) {
				println(0, str);
			}

			public void println(int level) {
				if (StaticVars.outputLevel >= level) {
					if (StaticVars.outputLevel == 0) {
						println(System.out);
					} else {
						println(System.out);
					}
				}
			}

			public void println(int level, Object str) {
				if (StaticVars.outputLevel >= level) {
					final String time = getTimeString();
					if (StaticVars.outputLevel == 0) {
						println(System.out, "[" + time + "] " + str);
					} else {
						println(System.out, "[" + time + "] " + str);
					}
				}
			}

			public void print(int level, String str) {
				if (StaticVars.outputLevel >= level) {
					if (StaticVars.outputLevel == 0) {
						print(System.out, str);
					} else {
						print(System.out, str);
					}
				}
			}

			public void println(int level, String prefix, String str) {
				if (StaticVars.outputLevel >= level) {
					final String time = getTimeString();
					if (StaticVars.outputLevel == 0) {
						println(System.out, "[" + time + "][" + prefix + "] " + str);
					} else {
						println(System.out, "[" + time + "][" + prefix + "] " + str);
					}
				}
			}

			public void println(int level, String... parts) {
				if (StaticVars.outputLevel >= level) {
					final String time = getTimeString();
					StringBuilder output = new StringBuilder();
					for (int i = 0; i < parts.length; i++) {
						if (i + 1 == parts.length) {
							output.append(' ');
							output.append(parts[i]);
						} else {
							output.append('[');
							output.append(parts[i]);
							output.append(']');
						}
					}
					output.insert(0, '[');
					output.insert(1, time);
					output.insert(time.length() + 1, ']');
					if (StaticVars.outputLevel == 0) {
						println(System.out, output.toString());
					} else {
						println(System.out, output.toString());
					}
				}
			}

			private String getTimeString() {
				return System.currentTimeMillis()+"";
			}
		};
	}
	
	@Override
	public AdvancedOutputStream out() {
		return os;
	}

}
