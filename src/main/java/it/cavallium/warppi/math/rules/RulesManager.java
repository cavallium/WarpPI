package it.cavallium.warppi.math.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.cavallium.warppi.ConsoleUtils;
import it.cavallium.warppi.Error;
import it.cavallium.warppi.PlatformUtils;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.ZipUtils;
import it.cavallium.warppi.deps.DJDTCompiler;
import it.cavallium.warppi.deps.DStandardOpenOption;
import it.cavallium.warppi.deps.DSystem;
import it.cavallium.warppi.deps.DURLClassLoader;
import it.cavallium.warppi.deps.StorageUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.solver.MathSolver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RulesManager {

	public static ObjectArrayList<Rule>[] rules;

	private RulesManager() {}

	@SuppressWarnings({ "unchecked", "unused" })
	public static void initialize() {
		ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Loading the rules");
		rules = new ObjectArrayList[RuleType.values().length];
		for (final RuleType val : RuleType.values()) {
			rules[val.ordinal()] = new ObjectArrayList<>();
		}
		try {
			boolean compiledSomething = false;
			InputStream defaultRulesList;
			try {
				defaultRulesList = StorageUtils.getResourceStream("/default-rules.lst");
			} catch (IOException ex) {
				throw new FileNotFoundException("default-rules.lst not found!");
			}
			final List<String> ruleLines = new ArrayList<>();
			final File rulesPath = StorageUtils.get("rules/");
			if (rulesPath.exists()) {
				for (File f : StorageUtils.walk(rulesPath)) {
					if (f.toString().endsWith(".java")) {
						String path = StorageUtils.relativize(rulesPath, f).toString();
						path = path.substring(0, path.length() - ".java".length());
						ruleLines.add(path);
						ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Found external rule: " + f.getAbsolutePath());
					}
				}
			}
			ruleLines.addAll(StorageUtils.readAllLines(defaultRulesList));

			final File tDir = StorageUtils.resolve(StorageUtils.get(System.getProperty("java.io.tmpdir"), "WarpPi-Calculator"), "rules-rt");
			//				try {
			//				final Path defaultResource = Utils.getResource("/math-rules-cache.zip");
			//			}
			InputStream cacheFileStream = null;
			File cacheFilePath = null;
			cacheFilePath = new File("math-rules-cache.zip");
			boolean cacheFileExists = false;
			if (PlatformUtils.isJavascript) {
				PlatformUtils.loadPlatformRules();
			} else {
				if (cacheFilePath.exists()) {
					cacheFileExists = true;
					cacheFileStream = new FileInputStream(cacheFilePath);
				} else {
					try {
						cacheFileStream = StorageUtils.getResourceStream("/math-rules-cache.zip");//Paths.get(Utils.getJarDirectory().toString()).resolve("math-rules-cache.zip").toAbsolutePath(
						org.apache.commons.io.FileUtils.copyInputStreamToFile(cacheFileStream, cacheFilePath);
						cacheFileExists = true;
					} catch (IOException ex) { //File does not exists.
					}
				}
				boolean useCache = false;
				if (cacheFileExists) {
					try {
						if (tDir.exists()) {
							tDir.delete();
						}
						ZipUtils.unzip(cacheFilePath.toString(), tDir.getParent().toString(), "");
						useCache = !StaticVars.startupArguments.isUncached();
					} catch (final Exception ex) {
						ex.printStackTrace();
					}
				}

				for (final String rulesLine : ruleLines) {
					if (rulesLine.length() > 0) {
						final String[] ruleDetails = rulesLine.split(",", 1);
						final String ruleName = ruleDetails[0];
						final String ruleNameEscaped = ruleName.replace(".", "_");
						ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Evaluating /rules/" + ruleNameEscaped + ".java");
						final String pathWithoutExtension = "/rules/" + ruleNameEscaped;
						final String scriptFile = pathWithoutExtension + ".java";
						final InputStream resourcePath = StorageUtils.getResourceStream(scriptFile);
						if (resourcePath == null) {
							System.err.println(new FileNotFoundException("/rules/" + ruleName + ".java not found!"));
						} else {
							Rule r = null;
							if (useCache) {
								try {
									ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "Trying to load cached rule");
									r = loadClassRuleFromSourceFile(scriptFile, tDir);
									if (r != null) {
										ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "Loaded cached rule");
									}
								} catch (final Exception e) {
									e.printStackTrace();
									ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", ruleName, "Can't load the rule!");
								}
							}
							if (r == null || !useCache) {
								ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "This rule is not cached. Compiling");
								try {
									r = compileJavaRule(scriptFile, tDir);
									compiledSomething = true;
								} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
									e.printStackTrace();
								}

							}
							if (r != null) {
								RulesManager.addRule(r);
							}
						}
					}
				}
			}
			ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Loaded all the rules successfully");
			if (!PlatformUtils.isJavascript && compiledSomething) {
				if (cacheFileExists || cacheFilePath.exists()) {
					cacheFilePath.delete();
				}
				ZipUtils.zip(tDir.toString(), cacheFilePath.toString(), "");
				ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Cached the compiled rules");
			}
			if (cacheFileStream != null) {
				cacheFileStream.close();
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			DSystem.exit(1);
		}
	}

	public static Rule compileJavaRule(String scriptFile, File tDir) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		final InputStream resource = StorageUtils.getResourceStream(scriptFile);
		final String text = StorageUtils.read(resource);
		final String[] textArray = text.split("\\n", 6);
		if (textArray[3].contains("PATH=")) {
			final String javaClassDeclaration = textArray[3].substring(6);
			int extIndex = javaClassDeclaration.lastIndexOf('.');
			final String javaClassNameOnly = javaClassDeclaration.substring(extIndex + 1, javaClassDeclaration.length());
			final String javaClassNameAndPath = new StringBuilder("it.cavallium.warppi.math.rules.").append(javaClassDeclaration).toString();
			extIndex = javaClassNameAndPath.lastIndexOf('.');
			final String javaCode = new StringBuilder("package ").append(javaClassNameAndPath.substring(0, extIndex >= 0 ? extIndex : javaClassNameAndPath.length())).append(";\n").append(textArray[5]).toString();
			final File tDirPath = StorageUtils.getParent(StorageUtils.resolve(tDir, javaClassNameAndPath.replace('.', File.separatorChar)));
			final File tFileJava = StorageUtils.resolve(tDirPath, javaClassNameOnly + ".java");
			final File tFileClass = StorageUtils.resolve(tDirPath, javaClassNameOnly + ".class");
			if (!tDirPath.exists()) {
				StorageUtils.createDirectories(tDirPath);
			}
			if (tFileJava.exists()) {
				tFileJava.delete();
			}
			StorageUtils.write(tFileJava, javaCode.getBytes("UTF-8"), DStandardOpenOption.WRITE, DStandardOpenOption.CREATE);
			final boolean compiled = DJDTCompiler.compile(new String[] { "-nowarn", "-1.8", tFileJava.toString() }, new PrintWriter(System.out), new PrintWriter(System.err));
			if (StaticVars.startupArguments.isUncached()) {
				tFileJava.deleteOnExit();
			} else {
				tFileJava.delete();
			}
			if (compiled) {
				tFileClass.deleteOnExit();
				return loadClassRuleDirectly(javaClassNameAndPath, tDir);
			} else {
				throw new IOException("Can't build script file '" + scriptFile + "'");
			}
		} else {
			throw new IOException("Can't build script file '" + scriptFile + "', the header is missing or wrong.");
		}
	}

	public static Rule loadClassRuleFromSourceFile(String scriptFile, File tDir) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		final InputStream resource = StorageUtils.getResourceStream(scriptFile);
		final String text = StorageUtils.read(resource);
		final String[] textArray = text.split("\\n", 6);
		if (textArray[3].contains("PATH=")) {
			final String javaClassName = textArray[3].substring(6);
			System.err.println(javaClassName);
			final String javaClassNameAndPath = new StringBuilder("it.cavallium.warppi.math.rules.").append(javaClassName).toString();
			try {
				return loadClassRuleDirectly(javaClassNameAndPath, tDir);
			} catch (final Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			throw new IOException("Can't load script file '" + scriptFile + "', the header is missing or wrong.");
		}
	}

	public static Rule loadClassRuleDirectly(String javaClassNameAndPath, File tDir) throws IOException,
			URISyntaxException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		final DURLClassLoader cl = new DURLClassLoader(new URL[] { tDir.toURI().toURL() });
		final Class<?> aClass = cl.loadClass(javaClassNameAndPath);
		cl.close();
		return (Rule) aClass.newInstance();
	}

	public static void warmUp() throws Error, InterruptedException {
		ObjectArrayList<Function> uselessResult = null;
		boolean uselessVariable = false;
		for (final RuleType val : RuleType.values()) {
			final ObjectArrayList<Rule> ruleList = rules[val.ordinal()];
			for (final Rule rule : ruleList) {
				String ruleName = "<null>";
				try {
					ruleName = rule.getRuleName();
					final ObjectArrayList<Function> uselessResult2 = rule.execute(generateUselessExpression());
					uselessVariable = (uselessResult == null ? new ObjectArrayList<>() : uselessResult).equals(uselessResult2);
					uselessResult = uselessResult2;
				} catch (final Exception e) {
					if (uselessVariable || true) {
						System.err.println("Exception thrown by rule '" + ruleName + "'!");
						e.printStackTrace();
					}
				}
			}
		}
		try {
			new MathSolver(generateUselessExpression()).solveAllSteps();
		} catch (InterruptedException | Error e) {
			e.printStackTrace();
		}
	}

	private static Function generateUselessExpression() {
		final MathContext mc = new MathContext();
		Function expr = new Expression(mc);
		expr = expr.setParameter(0, new Variable(mc, 'x', V_TYPE.VARIABLE));
		return expr;
	}

	public static void addRule(Rule rule) {
		rules[rule.getRuleType().ordinal()].add(rule);
		ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", rule.getRuleName(), "Loaded as " + rule.getRuleType() + " rule");
	}
}
