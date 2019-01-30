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

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.Platform.StorageUtils;
import it.cavallium.warppi.Platform.URLClassLoader;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.rules.dsl.DslAggregateException;
import it.cavallium.warppi.math.rules.dsl.RulesDsl;
import it.cavallium.warppi.math.solver.MathSolver;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RulesManager {

	public static ObjectArrayList<Rule>[] rules;

	private RulesManager() {}

	@SuppressWarnings({ "unchecked" })
	public static void initialize() {
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Loading the rules");
		RulesManager.rules = new ObjectArrayList[RuleType.values().length];
		for (final RuleType val : RuleType.values()) {
			RulesManager.rules[val.ordinal()] = new ObjectArrayList<>();
		}

		try {
			loadDslRules();

			boolean compiledSomething = false;
			InputStream defaultRulesList;
			try {
				defaultRulesList = Engine.getPlatform().getStorageUtils().getResourceStream("/default-rules.lst");
			} catch (final IOException ex) {
				throw new FileNotFoundException("default-rules.lst not found!");
			}
			final List<String> ruleLines = new ArrayList<>();
			final File rulesPath = Engine.getPlatform().getStorageUtils().get("rules/");
			if (rulesPath.exists()) {
				for (final File f : Engine.getPlatform().getStorageUtils().walk(rulesPath)) {
					if (f.toString().endsWith(".java")) {
						String path = Engine.getPlatform().getStorageUtils().relativize(rulesPath, f).toString();
						path = path.substring(0, path.length() - ".java".length());
						ruleLines.add(path);
						Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Found external rule: " + f.getAbsolutePath());
					}
				}
			}
			ruleLines.addAll(Engine.getPlatform().getStorageUtils().readAllLines(defaultRulesList));

			final File tDir = Engine.getPlatform().getStorageUtils().resolve(Engine.getPlatform().getStorageUtils().get(System.getProperty("java.io.tmpdir"), "WarpPi-Calculator"), "rules-rt");
			//				try {
			//				final Path defaultResource = Utils.getResource("/math-rules-cache.zip");
			//			}
			InputStream cacheFileStream = null;
			File cacheFilePath = null;
			cacheFilePath = new File("math-rules-cache.zip");
			boolean cacheFileExists = false;
			if (Engine.getPlatform().isJavascript()) {
				Engine.getPlatform().loadPlatformRules();
			} else {
				if (cacheFilePath.exists()) {
					cacheFileExists = true;
					cacheFileStream = new FileInputStream(cacheFilePath);
				} else {
					try {
						cacheFileStream = Engine.getPlatform().getStorageUtils().getResourceStream("/math-rules-cache.zip");//Paths.get(Utils.getJarDirectory().toString()).resolve("math-rules-cache.zip").toAbsolutePath(
						org.apache.commons.io.FileUtils.copyInputStreamToFile(cacheFileStream, cacheFilePath);
						cacheFileExists = true;
					} catch (final IOException ex) { //File does not exists.
					}
				}
				boolean useCache = false;
				if (cacheFileExists) {
					try {
						if (tDir.exists()) {
							tDir.delete();
						}
						Engine.getPlatform().unzip(cacheFilePath.toString(), tDir.getParent().toString(), "");
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
						Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", "Evaluating /rules/" + ruleNameEscaped + ".java");
						final String pathWithoutExtension = "/rules/" + ruleNameEscaped;
						final String scriptFile = pathWithoutExtension + ".java";
						final InputStream resourcePath = Engine.getPlatform().getStorageUtils().getResourceStream(scriptFile);
						if (resourcePath == null) {
							System.err.println(new FileNotFoundException("/rules/" + ruleName + ".java not found!"));
						} else {
							Rule r = null;
							if (useCache) {
								try {
									Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "Trying to load cached rule");
									r = RulesManager.loadClassRuleFromSourceFile(scriptFile, tDir);
									if (r != null) {
										Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "Loaded cached rule");
									}
								} catch (final Exception e) {
									e.printStackTrace();
									Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", ruleName, "Can't load the rule " + ruleNameEscaped + "!");
								}
							}
							if (r == null || !useCache) {
								Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", ruleName, "This rule is not cached. Compiling");
								try {
									r = RulesManager.compileJavaRule(scriptFile, tDir);
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
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Loaded all the rules successfully");
			if (!Engine.getPlatform().isJavascript() && compiledSomething) {
				if (cacheFileExists || cacheFilePath.exists()) {
					cacheFilePath.delete();
				}
				Engine.getPlatform().zip(tDir.toString(), cacheFilePath.toString(), "");
				Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Cached the compiled rules");
			}
			if (cacheFileStream != null) {
				cacheFileStream.close();
			}
		} catch (URISyntaxException | IOException | DslAggregateException e) {
			e.printStackTrace();
			Engine.getPlatform().exit(1);
		}
	}

	private static void loadDslRules() throws IOException, DslAggregateException {
		final StorageUtils storageUtils = Engine.getPlatform().getStorageUtils();

		final File dslRulesPath = storageUtils.get("rules/dsl/");
		if (!dslRulesPath.exists()) {
			return;
		}

		for (final File file : storageUtils.walk(dslRulesPath)) {
			if (!file.toString().endsWith(".rules")) {
				continue;
			}

			Engine.getPlatform().getConsoleUtils().out().println(
					ConsoleUtils.OUTPUTLEVEL_NODEBUG,
					"RulesManager",
					"Found DSL rules file: " + file.getAbsolutePath()
			);

			final String source;
			try (final InputStream resource = storageUtils.getResourceStream(file.toString())) {
				source = storageUtils.read(resource);
			}

			RulesDsl.makeRules(source).forEach(RulesManager::addRule);
		}
	}

	public static Rule compileJavaRule(final String scriptFile, final File tDir) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		final InputStream resource = Engine.getPlatform().getStorageUtils().getResourceStream(scriptFile);
		final String text = Engine.getPlatform().getStorageUtils().read(resource);
		final String[] textArray = text.split("\\n", 6);
		if (textArray[3].contains("PATH=")) {
			final String javaClassDeclaration = textArray[3].substring(6);
			int extIndex = javaClassDeclaration.lastIndexOf('.');
			final String javaClassNameOnly = javaClassDeclaration.substring(extIndex + 1, javaClassDeclaration.length());
			final String javaClassNameAndPath = new StringBuilder("it.cavallium.warppi.math.rules.").append(javaClassDeclaration).toString();
			extIndex = javaClassNameAndPath.lastIndexOf('.');
			final String javaCode = new StringBuilder("package ").append(javaClassNameAndPath.substring(0, extIndex >= 0 ? extIndex : javaClassNameAndPath.length())).append(";\n").append(textArray[5]).toString();
			final File tDirPath = Engine.getPlatform().getStorageUtils().getParent(Engine.getPlatform().getStorageUtils().resolve(tDir, javaClassNameAndPath.replace('.', File.separatorChar)));
			final File tFileJava = Engine.getPlatform().getStorageUtils().resolve(tDirPath, javaClassNameOnly + ".java");
			final File tFileClass = Engine.getPlatform().getStorageUtils().resolve(tDirPath, javaClassNameOnly + ".class");
			if (!tDirPath.exists()) {
				Engine.getPlatform().getStorageUtils().createDirectories(tDirPath);
			}
			if (tFileJava.exists()) {
				tFileJava.delete();
			}
			Engine.getPlatform().getStorageUtils();
			Engine.getPlatform().getStorageUtils();
			Engine.getPlatform().getStorageUtils().write(tFileJava, javaCode.getBytes("UTF-8"), StorageUtils.OpenOptionWrite, StorageUtils.OpenOptionCreate);
			final boolean compiled = Engine.getPlatform().compile(new String[] { "-nowarn", "-1.8", "-proc:none", tFileJava.toString() }, new PrintWriter(System.out), new PrintWriter(System.err));
			if (StaticVars.startupArguments.isUncached()) {
				tFileJava.deleteOnExit();
			} else {
				tFileJava.delete();
			}
			if (compiled) {
				tFileClass.deleteOnExit();
				return RulesManager.loadClassRuleDirectly(javaClassNameAndPath, tDir);
			} else {
				throw new IOException("Can't build script file '" + scriptFile + "'");
			}
		} else {
			throw new IOException("Can't build script file '" + scriptFile + "', the header is missing or wrong.");
		}
	}

	public static Rule loadClassRuleFromSourceFile(final String scriptFile, final File tDir) throws IOException,
			URISyntaxException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		final InputStream resource = Engine.getPlatform().getStorageUtils().getResourceStream(scriptFile);
		final String text = Engine.getPlatform().getStorageUtils().read(resource);
		final String[] textArray = text.split("\\n", 6);
		if (textArray[3].contains("PATH=")) {
			final String javaClassName = textArray[3].substring(6);
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "RulesManager", "Rule java class name: " + javaClassName);
			final String javaClassNameAndPath = new StringBuilder("it.cavallium.warppi.math.rules.").append(javaClassName).toString();
			try {
				return RulesManager.loadClassRuleDirectly(javaClassNameAndPath, tDir);
			} catch (final Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			throw new IOException("Can't load script file '" + scriptFile + "', the header is missing or wrong.");
		}
	}

	public static Rule loadClassRuleDirectly(final String javaClassNameAndPath, final File tDir) throws IOException,
			URISyntaxException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		final URLClassLoader cl = Engine.getPlatform().newURLClassLoader(new URL[] { tDir.toURI().toURL() });
		final Class<?> aClass = cl.loadClass(javaClassNameAndPath);
		cl.close();
		return (Rule) aClass.newInstance();
	}

	public static void warmUp() throws Error, InterruptedException {
		ObjectArrayList<Function> uselessResult = null;
		boolean uselessVariable = false;
		for (final RuleType val : RuleType.values()) {
			final ObjectArrayList<Rule> ruleList = RulesManager.rules[val.ordinal()];
			for (final Rule rule : ruleList) {
				String ruleName = "<null>";
				try {
					ruleName = rule.getRuleName();
					final ObjectArrayList<Function> uselessResult2 = rule.execute(RulesManager.generateUselessExpression());
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
			new MathSolver(RulesManager.generateUselessExpression()).solveAllSteps();
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

	public static void addRule(final Rule rule) {
		RulesManager.rules[rule.getRuleType().ordinal()].add(rule);
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", rule.getRuleName(), "Loaded as " + rule.getRuleType() + " rule");
	}
}
