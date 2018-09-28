package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockParenthesis;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Expression extends FunctionSingle {

	public Expression(final MathContext root) {
		super(root);
	}

	public Expression(final MathContext root, final Function value) {
		super(root, value);
	}

	private final boolean initialParenthesis = false;

	@Deprecated
	public Expression(final MathContext root, final String string) throws Error {
		this(root, string, "", true);
	}

	@Deprecated
	public Expression(final MathContext root, final String string, final String debugSpaces, final boolean initialParenthesis) throws Error {
		super(root);

		/*
		super(root);
		this.initialParenthesis = initialParenthesis;
		boolean isNumber = false;
		
		// Determine if the expression is already a number:
		// Determina se l'espressione è già un numero:
		try {
			new Number(root, string);
			isNumber = true;
		} catch (final NumberFormatException ex) {
			isNumber = false;
		}
		
		String processExpression = string;
		Utils.debug.println(debugSpaces + "•Analyzing expression:" + processExpression);
		
		isNumber = false; //TODO: rimuovere isNumber, alcune semplificazione come la divisione per zero altrimenti verrebbero saltate.
		
		if (isNumber) {
			// If the expression is already a number:
			// Se l'espressione è già un numero:
			final Number t = new Number(root, string);
			parameter = t;
			Utils.debug.println(debugSpaces + "•Result:" + t.toString());
		} else {
			// Else prepare the expression:
			// Altrimenti prepara l'espressione:
			debugSpaces += "  ";
		
			// IF the expression is not a number:
			// Se l'espressione non è già un numero:
		
			// Check if there are more than one equal symbol (=)
			// Controlla se ci sono più di un uguale (=)
			int equationsFound = 0;
			int systemsFound = 0;
			for (final char c : processExpression.toCharArray()) {
				if (("" + c).equals(MathematicalSymbols.EQUATION)) {
					equationsFound += 1;
				}
				if (("" + c).equals(MathematicalSymbols.SYSTEM)) {
					equationsFound += 1;
				}
			}
			if (equationsFound == 1 && systemsFound == 0) {
				processExpression = MathematicalSymbols.SYSTEM + processExpression;
				systemsFound += 1;
			}
			if (equationsFound != systemsFound) {
				throw new Error(Errors.SYNTAX_ERROR);
			}
		
			//Solve the exceeding symbols ++ and --
			// Correggi i segni ++ e -- in eccesso
			Pattern pattern = Pattern.compile("\\+\\++?|\\-\\-+?");
			Matcher matcher = pattern.matcher(processExpression);
			boolean symbolsChanged = false;
			while (matcher.find()) {
				symbolsChanged = true;
				final String correzione = "+";
				processExpression = processExpression.substring(0, matcher.start(0)) + correzione + processExpression.substring(matcher.start(0) + matcher.group(0).length(), processExpression.length());
				matcher = pattern.matcher(processExpression);
			}
		
			// Correct the exceeding symbols +- and -+
			// Correggi i segni +- e -+ in eccesso
			pattern = Pattern.compile("\\+\\-|\\-\\+");
			matcher = pattern.matcher(processExpression);
			while (matcher.find()) {
				symbolsChanged = true;
				final String correzione = "-";
				processExpression = processExpression.substring(0, matcher.start(0)) + correzione + processExpression.substring(matcher.start(0) + matcher.group(0).length(), processExpression.length());
				matcher = pattern.matcher(processExpression);
			}
		
			// Rimuovi i segni appena dopo le parentesi
			if (processExpression.contains("(+")) {
				symbolsChanged = true;
				processExpression = processExpression.replace("(+", "(");
			}
		
		//			// Cambia i segni appena prima le parentesi
		//			if (processExpression.contains("-(")) {
		//				symbolsChanged = true;
		//				processExpression = processExpression.replace("-(", "-1*(");
		//			}
		
			// Rimuovi i segni appena dopo l'inizio
			if (processExpression.startsWith("+")) {
				symbolsChanged = true;
				processExpression = processExpression.substring(1, processExpression.length());
			}
		
			// Rimuovi i + in eccesso
			pattern = Pattern.compile("[" + ArrayToRegex(Utils.add(concat(MathematicalSymbols.signums(true), MathematicalSymbols.functions), '(')) + "]\\+[^" + ArrayToRegex(concat(concat(MathematicalSymbols.signums(true), MathematicalSymbols.functions), new char[] { '(', ')' })) + "]+?[" + ArrayToRegex(concat(MathematicalSymbols.signums(true), MathematicalSymbols.functions)) + "]|[" + ArrayToRegex(concat(MathematicalSymbols.signums(true), MathematicalSymbols.functions)) + "]+?\\+[^" + ArrayToRegex(concat(concat(MathematicalSymbols.signums(true), MathematicalSymbols.functions), new char[] { '(', ')' })) + "]");
			matcher = pattern.matcher(processExpression);
			symbolsChanged = false;
			while (matcher.find()) {
				symbolsChanged = true;
				final String correzione = matcher.group(0).replaceFirst(Matcher.quoteReplacement("+"), "");
				processExpression = processExpression.substring(0, matcher.start(0) + 1) + correzione + processExpression.substring(matcher.start(0) + matcher.group(0).length(), processExpression.length());
				matcher = pattern.matcher(processExpression);
			}
		
			// Correggi i segni - in −
			processExpression = processExpression.replace('-', MathematicalSymbols.SUBTRACTION);
		
			// Correggi i segni − dopo di espressioni o funzioni SN in -
			pattern = Pattern.compile("[" + Utils.ArrayToRegex(concat(concat(MathematicalSymbols.functions, new char[] { MathematicalSymbols.PARENTHESIS_OPEN }), MathematicalSymbols.signums(true))) + "]" + MathematicalSymbols.SUBTRACTION);
			matcher = pattern.matcher(processExpression);
			while (matcher.find()) {
				symbolsChanged = true;
				final char correzione = MathematicalSymbols.MINUS;
				processExpression = processExpression.substring(0, matcher.start(0) + 1) + correzione + processExpression.substring(matcher.start(0) + 2, processExpression.length());
				matcher = pattern.matcher(processExpression);
			}
		
			// Cambia il segno iniziale − in -
			if (processExpression.startsWith("−")) {
				symbolsChanged = true;
				processExpression = "-" + processExpression.substring(1, processExpression.length());
			}
		
			if (symbolsChanged) {
				Utils.debug.println(debugSpaces + "•Resolved signs:" + processExpression);
			}
		
		//			// Aggiungi le parentesi implicite per le potenze con una incognita
		//			pattern = Pattern.compile("(?<!(?:\\(|^))(["+Utils.ArrayToRegex(MathematicalSymbols.variables())+"]+"+MathematicalSymbols.POWER+"[^" + Utils.ArrayToRegex(Utils.add(concat(MathematicalSymbols.functionsNSN(), concat(MathematicalSymbols.signums(true), MathematicalSymbols.genericSyntax())), ")")) + "])(?!\\))");
		//			matcher = pattern.matcher(processExpression);
		//			symbolsChanged = false;
		//			while (matcher.find()) {
		//				symbolsChanged = true;
		//				String correzione = matcher.group().replace(MathematicalSymbols.POWER, "⑴");
		//				processExpression = processExpression.substring(0, matcher.start(0)) + correzione + processExpression.substring(matcher.start(0) + matcher.group(0).length(), processExpression.length());
		//				matcher = pattern.matcher(processExpression);
		//			}
		//
		//			processExpression = processExpression.replace("⑴", MathematicalSymbols.POWER);
		
			// Aggiungi i segni * accanto alle parentesi
			pattern = Pattern.compile("\\([^\\(]+?\\)");
			matcher = pattern.matcher(processExpression);
			while (matcher.find()) {
				symbolsChanged = true;
				// sistema i segni * impliciti prima e dopo l'espressione.
				String beforeexp = processExpression.substring(0, matcher.start(0));
				final String newexp = matcher.group(0).substring(1, matcher.group(0).length() - 1);
				String afterexp = processExpression.substring(matcher.start(0) + matcher.group(0).length(), processExpression.length());
				if (Pattern.compile("[^\\-" + Utils.ArrayToRegex(Utils.add(concat(MathematicalSymbols.functions, concat(MathematicalSymbols.signums(true), MathematicalSymbols.genericSyntax)), '(')) + "]$").matcher(beforeexp).find()) {
					// Se la stringa precedente finisce con un numero
					beforeexp += MathematicalSymbols.MULTIPLICATION;
				}
				if (Pattern.compile("^[^\\-" + Utils.ArrayToRegex(Utils.add(concat(MathematicalSymbols.functions, concat(MathematicalSymbols.signums(true), MathematicalSymbols.genericSyntax)), ')')) + "]").matcher(afterexp).find()) {
					// Se la stringa successiva inizia con un numero
					afterexp = MathematicalSymbols.MULTIPLICATION + afterexp;
				}
				processExpression = beforeexp + "⑴" + newexp + "⑵" + afterexp;
				matcher = pattern.matcher(processExpression);
			}
		
			processExpression = processExpression.replace("⑴", "(").replace("⑵", ")");
		
			if (symbolsChanged) {
				Utils.debug.println(debugSpaces + "•Added implicit multiplications:" + processExpression);
			}
		
			Utils.debug.println(debugSpaces + "•Subdivision in classes:");
		
			debugSpaces += "  ";
		
			// Convert the expression to a list of objects
			Expression imputRawParenthesis = new Expression(root);
			imputRawParenthesis = (Expression) imputRawParenthesis.setParameter(null);
			String tmp = "";
			final char[] functions = concat(concat(concat(concat(MathematicalSymbols.functions, MathematicalSymbols.parentheses), MathematicalSymbols.signums(true)), MathematicalSymbols.variables), MathematicalSymbols.genericSyntax);
			for (int i = 0; i < processExpression.length(); i++) {
				// Per ogni carattere cerca se è un numero o una funzione:
				final char charI = processExpression.charAt(i);
				if (Utils.isInArray(charI, functions)) {
		
					// Finds the type of function fron the following list
					// Cerca il tipo di funzione tra le esistenti
					Function f = null;
					switch (charI) {
						case MathematicalSymbols.SUM:
							f = new Sum(root, null, null);
							break;
						case MathematicalSymbols.SUM_SUBTRACTION:
							f = new SumSubtraction(root, null, null);
							break;
						case MathematicalSymbols.SUBTRACTION:
							f = new Subtraction(root, null, null);
							break;
						case MathematicalSymbols.MINUS:
							f = new Negative(root, null);
							break;
						case MathematicalSymbols.MULTIPLICATION:
							f = new Multiplication(root, null, null);
							break;
						case MathematicalSymbols.DIVISION:
							f = new Division(root, null, null);
							break;
						case MathematicalSymbols.NTH_ROOT:
							f = new Root(root, null, null);
							break;
						case MathematicalSymbols.SQUARE_ROOT:
							f = new RootSquare(root, null);
							break;
						case MathematicalSymbols.POWER:
							f = new Power(root, null, null);
							break;
						case MathematicalSymbols.SINE:
							f = new Sine(root, null);
							break;
						case MathematicalSymbols.COSINE:
							f = new Cosine(root, null);
							break;
						case MathematicalSymbols.TANGENT:
							f = new Tangent(root, null);
							break;
						case MathematicalSymbols.ARC_SINE:
							f = new ArcSine(root, null);
							break;
						case MathematicalSymbols.ARC_COSINE:
							f = new ArcCosine(root, null);
							break;
						case MathematicalSymbols.ARC_TANGENT:
							f = new ArcTangent(root, null);
							break;
						case MathematicalSymbols.PARENTHESIS_OPEN:
							// Find the last closed parenthesis
							// cerca l'ultima parentesi chiusa
							int startIndex = i;
							int endIndex = -1;
							int jumps = -1;
							for (int i2 = startIndex; i2 < processExpression.length(); i2++) {
								if ((processExpression.charAt(i2) + "").equals(MathematicalSymbols.PARENTHESIS_CLOSE)) {
									if (jumps == 0) {
										endIndex = i2;
										break;
									} else if (jumps > 0) {
										jumps -= 1;
									} else if (jumps < 0) {
										throw new Error(Errors.UNBALANCED_STACK);
									}
								} else if ((processExpression.charAt(i2) + "").equals(MathematicalSymbols.PARENTHESIS_OPEN)) {
									jumps += 1;
								}
							}
							if (endIndex == -1 || endIndex < startIndex) {
								throw new Error(Errors.UNBALANCED_STACK);
							}
							startIndex += 1;
							i = startIndex;
		
							String tmpExpr = "";
							while (i < endIndex) {
								tmpExpr += processExpression.charAt(i);
								i++;
							}
							f = new Expression(root, tmpExpr, debugSpaces, false);
							break;
						default:
							if (Utils.isInArray(charI, MathematicalSymbols.variables)) {
								f = new Variable(root, charI, Variable.V_TYPE.UNKNOWN);
							} else {
								if (charI == '(' || charI == ')') {
									throw new Error(Errors.UNBALANCED_STACK);
								} else {
									System.err.println("Unexpected character while parsing expression: " + charI);
									throw new Error(Errors.SYNTAX_ERROR);
								}
		//								throw new java.lang.RuntimeException("Il carattere " + charI + " non è tra le funzioni designate!\nAggiungerlo ad esse o rimuovere il carattere dall'espressione!");
							}
					}
					if (f instanceof Expression) {
						tmp = "";
					} else if (f instanceof Variable) {
						if (imputRawParenthesis.getParametersLength() == 0) {
							if (tmp.length() > 0) {
								imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Number(root, tmp));
								Utils.debug.println(debugSpaces + "•Added number to expression:" + tmp);
								imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Multiplication(root, null, null));
								Utils.debug.println(debugSpaces + "•Added multiplication to expression:" + new Multiplication(root, null, null).getClass().getSimpleName());
							}
						} else {
							final Function precedentFunction = imputRawParenthesis.getParameter(imputRawParenthesis.getParametersLength() - 1);
							if (tmp.length() > 0) {
								if (precedentFunction instanceof Number || precedentFunction instanceof Variable) {
									imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Multiplication(root, null, null));
									Utils.debug.println(debugSpaces + "•Added multiplication to expression:" + new Multiplication(root, null, null).getClass().getSimpleName());
								}
								if (tmp.equals("-")) {
									imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Subtraction(root, null, null));
								} else {
									imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Number(root, tmp));
									Utils.debug.println(debugSpaces + "•Added number to expression:" + tmp);
								}
							}
							if (tmp.length() > 0 || (precedentFunction instanceof Number || precedentFunction instanceof Variable)) {
								imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Multiplication(root, null, null));
								Utils.debug.println(debugSpaces + "•Added multiplication to expression:" + new Multiplication(root, null, null).getClass().getSimpleName());
							}
						}
					} else {
						if (tmp.length() != 0) {
							if (tmp.equals("-")) {
								if (tmp.equals("-")) {
									tmp = "-1";
								}
							}
							imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Number(root, tmp));
							Utils.debug.println(debugSpaces + "•Added number to expression:" + tmp);
						}
					}
					imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(f);
					Utils.debug.println(debugSpaces + "•Added variable to expression:" + f.getClass().getSimpleName() + (f instanceof Number ? " (number)" : " (variable)"));
					tmp = "";
				} else {
					try {
						if (charI != '-' && charI != '.') {
							new BigDecimal(tmp + charI);
						}
						// Se il carattere è un numero intero, un segno
						// negativo, o un punto
						tmp += charI;
					} catch (final Exception exc) {
						throw new java.lang.RuntimeException("Il carattere " + tmp + charI + " non è nè un numero nè un espressione presente nella lista completa!\nAggiungerlo ad essa o rimuovere il carattere dall'espressione!");
					}
				}
			}
			if (tmp.length() > 0) {
				Utils.debug.println(debugSpaces + "•Added variable to expression:" + tmp);
				try {
					imputRawParenthesis = (Expression) imputRawParenthesis.appendParameter(new Number(root, tmp));
				} catch (final NumberFormatException ex) {
					throw new Error(Errors.SYNTAX_ERROR);
				}
				tmp = "";
			}
		
			int dsl = debugSpaces.length();
			debugSpaces = "";
			for (int i = 0; i < dsl - 2; i++) {
				debugSpaces += " ";
			}
			Utils.debug.println(debugSpaces + "•Finished the subdivision in classes.");
			// Fine suddivisione di insieme
		
			Utils.debug.println(debugSpaces + "•Removing useless parentheses");
			for (int i = 0; i < imputRawParenthesis.getParametersLength(); i++) {
				if (imputRawParenthesis.getParameter(i) instanceof Expression) {
					final Expression par = (Expression) imputRawParenthesis.getParameter(i);
					if (par.getParametersLength() == 1) {
						final Function subFunz = par.getParameter(0);
						if (subFunz instanceof Expression || subFunz instanceof Number || subFunz instanceof Variable) {
							imputRawParenthesis = (Expression) imputRawParenthesis.setParameter(i, subFunz);
							Utils.debug.println(debugSpaces + "  •Useless parentheses removed");
						}
					}
				}
			}
		
			// Inizia l'affinazione dell'espressione
			Utils.debug.println(debugSpaces + "•Pushing classes...");
		
			final Function[] oldFunctionsArray = imputRawParenthesis.getParameters();
			final ObjectArrayList<Function> oldFunctionsList = new ObjectArrayList<>();
			for (int i = 0; i < oldFunctionsArray.length; i++) {
				Function funzione = oldFunctionsArray[i];
				if (funzione != null) {
					//Affinazione
					if (funzione instanceof Root) {
						if ((i - 1) >= 0 && oldFunctionsArray[i - 1] instanceof Number && ((Number) oldFunctionsArray[i - 1]).getTerm().compareTo(new BigDecimal(2)) == 0) {
							oldFunctionsArray[i] = null;
							oldFunctionsArray[i - 1] = null;
							oldFunctionsList.remove(oldFunctionsList.size() - 1);
							i -= 1;
							funzione = new RootSquare(root, null);
						}
					}
					//Aggiunta della funzione alla lista grezza
					oldFunctionsList.add(funzione);
				}
			}
		
			if (oldFunctionsList.size() > 1) {
				Utils.debug.println(debugSpaces + "  •Correcting classes:");
		
				int before = 0;
				String step = "SN Functions";
				int n = 0;
				do {
					before = oldFunctionsList.size();
					int i = 0;
					boolean change = false;
					if (Utils.areThereOnlyEmptySNFunctions(oldFunctionsList)) {
						step = "SN Functions"; // SECONDA FASE
					} else if (Utils.areThereOnlyEmptyNSNFunctions(oldFunctionsList)) {
						step = "NSN Functions"; // TERZA FASE
					} else if (Utils.areThereEmptyMultiplications(oldFunctionsList)) {
						step = "multiplications"; // QUARTA FASE
					} else if (Utils.areThereEmptySums(oldFunctionsList)) {
						step = "sums"; // QUINTA FASE
					} else {
		//						fase = "errore";
						System.out.println("WARN: ---> POSSIBILE ERRORE????? <---");// BOH
		//						throw new Errore(Errori.SYNTAX_ERROR);
						while (oldFunctionsList.size() > 1) {
							oldFunctionsList.set(0, new Multiplication(root, oldFunctionsList.get(0), oldFunctionsList.remove(1)));
						}
					}
					Utils.debug.println(debugSpaces + "  •Phase: " + step);
					while (i < oldFunctionsList.size() && change == false && oldFunctionsList.size() > 1) {
						Function funzioneTMP = oldFunctionsList.get(i);
						if (funzioneTMP instanceof FunctionOperator) {
							if (step != "SN Functions") {
								if ((step == "sums" && (funzioneTMP instanceof Sum || funzioneTMP instanceof SumSubtraction || funzioneTMP instanceof Subtraction) == true && ((funzioneTMP instanceof FunctionSingle && ((FunctionSingle) funzioneTMP).getParameter() == null) || (funzioneTMP instanceof FunctionOperator && ((FunctionOperator) funzioneTMP).getParameter1() == null && ((FunctionOperator) funzioneTMP).getParameter2() == null) || (!(funzioneTMP instanceof FunctionSingle) && !(funzioneTMP instanceof FunctionOperator)))) || (step.equals("multiplications") && ((funzioneTMP instanceof Multiplication) || (funzioneTMP instanceof Division)) && ((FunctionOperator) funzioneTMP).getParameter1() == null && ((FunctionOperator) funzioneTMP).getParameter2() == null) || (step == "NSN Functions" && (funzioneTMP instanceof Sum) == false && (funzioneTMP instanceof SumSubtraction) == false && (funzioneTMP instanceof Subtraction) == false && (funzioneTMP instanceof Multiplication) == false && (funzioneTMP instanceof Division) == false && ((funzioneTMP instanceof FunctionSingle && ((FunctionSingle) funzioneTMP).getParameter() == null) || (funzioneTMP instanceof FunctionOperator && ((FunctionOperator) funzioneTMP).getParameter1() == null && ((FunctionOperator) funzioneTMP).getParameter2() == null) || (!(funzioneTMP instanceof FunctionSingle) && !(funzioneTMP instanceof FunctionOperator))))) {
									change = true;
		
									if (i + 1 < oldFunctionsList.size() && i - 1 >= 0) {
										funzioneTMP = ((FunctionOperator) funzioneTMP).setParameter1(oldFunctionsList.get(i - 1));
										funzioneTMP = ((FunctionOperator) funzioneTMP).setParameter2(oldFunctionsList.get(i + 1));
										oldFunctionsList.set(i, funzioneTMP);
		
										// è importante togliere prima gli elementi
										// in fondo e poi quelli davanti, perché gli
										// indici scalano da destra a sinistra.
										oldFunctionsList.remove(i + 1);
										oldFunctionsList.remove(i - 1);
		
										Utils.debug.println(debugSpaces + "  •Set variable to expression:" + funzioneTMP.getClass().getSimpleName());
										try {
											Utils.debug.println(debugSpaces + "    " + "var1=" + ((FunctionOperator) funzioneTMP).getParameter1().toString());
										} catch (final NullPointerException ex2) {}
										try {
											Utils.debug.println(debugSpaces + "    " + "var2=" + ((FunctionOperator) funzioneTMP).getParameter2().toString());
										} catch (final NullPointerException ex2) {}
										try {
											Utils.debug.println(debugSpaces + "    " + "(result)=" + ((FunctionOperator) funzioneTMP).toString());
										} catch (final NullPointerException ex2) {}
		
									} else {
										throw new Error(Errors.SYNTAX_ERROR);
									}
								}
							}
						} else if (funzioneTMP instanceof FunctionSingle) {
							if ((step == "SN Functions" && ((FunctionSingle) funzioneTMP).getParameter() == null)) {
								if (i + 1 < oldFunctionsList.size()) {
									final Function nextFunc = oldFunctionsList.get(i + 1);
									if (nextFunc instanceof FunctionSingle && ((FunctionSingle) nextFunc).getParameter() == null) {
		
									} else {
										change = true;
										funzioneTMP = ((FunctionSingle) funzioneTMP).setParameter(nextFunc);
										oldFunctionsList.set(i, funzioneTMP);
		
										// è importante togliere prima gli elementi in
										// fondo e poi quelli davanti, perché gli indici
										// scalano da destra a sinistra.
										oldFunctionsList.remove(i + 1);
		
										Utils.debug.println(debugSpaces + "  •Set variable to expression:" + funzioneTMP.getClass().getSimpleName());
										final Function var = ((FunctionSingle) funzioneTMP).getParameter();
										if (var == null) {
											Utils.debug.println(debugSpaces + "    " + "var=null");
										} else {
											Utils.debug.println(debugSpaces + "    " + "var=" + var.toString());
										}
									}
								} else {
									throw new Error(Errors.SYNTAX_ERROR);
								}
							}
						} else if (funzioneTMP instanceof Number || funzioneTMP instanceof Variable || funzioneTMP instanceof Expression) {
							if (n < 300) {
								// Utils.debug.println(debugSpaces+" •Set variable
								// to number:"+funzioneTMP.calcola());
							}
						} else {
							throw new java.lang.RuntimeException("Tipo sconosciuto");
						}
						i++;
						n++;
					}
				} while (((oldFunctionsList.size() != before || step != "sums") && oldFunctionsList.size() > 1));
			}
			if (oldFunctionsList.isEmpty()) {
				super.functions = new Function[] { new Number(root, 0) };
			} else {
				super.functions = oldFunctionsList.toArray(new Function[oldFunctionsList.size()]);
			}
		
			dsl = debugSpaces.length();
			debugSpaces = "";
			for (int i = 0; i < dsl - 2; i++) {
				debugSpaces += " ";
			}
			Utils.debug.println(debugSpaces + "•Finished correcting classes.");
		
			final String result = toString();
			Utils.debug.println(debugSpaces + "•Result:" + result);
		}
		*/
	}

	public boolean parenthesisNeeded() {
		boolean parenthesisneeded = true;
		if (initialParenthesis) {
			parenthesisneeded = false;
		} else {
			final Function f = getParameter(0);
			if (f instanceof Number || f instanceof Variable || f instanceof Expression || f instanceof Division || f instanceof Joke || f instanceof Undefined || f instanceof Power || f instanceof Sine || f instanceof Cosine || f instanceof Tangent || f instanceof ArcSine || f instanceof ArcCosine || f instanceof ArcTangent || f instanceof RootSquare) {
				parenthesisneeded = false;
			}
			if (f instanceof Multiplication) {
				if (((Multiplication) f).getParameter1() instanceof Number) {
					parenthesisneeded = !(((Multiplication) f).getParameter2() instanceof Variable);
				} else if (((Multiplication) f).getParameter2() instanceof Number) {
					parenthesisneeded = !(((Multiplication) f).getParameter1() instanceof Variable);
				} else if (((Multiplication) f).getParameter1() instanceof Variable || ((Multiplication) f).getParameter2() instanceof Variable) {
					parenthesisneeded = false;
				}
			}
		}
		return parenthesisneeded;
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final ObjectArrayList<Block> sub = getParameter(0).toBlock(context);
		final BlockParenthesis bp = new BlockParenthesis();
		final BlockContainer bpc = bp.getNumberContainer();
		for (final Block b : sub) {
			bpc.appendBlockUnsafe(b);
		}
		bpc.recomputeDimensions();
		bp.recomputeDimensions();
		result.add(bp);
		return result;
	}

	@Override
	public String toString() {
		String s = "(";
		if (parameter == null) {
			s += "null";
		} else {
			s += parameter.toString();
		}
		s += ")";
		return s;
	}

	@Override
	public boolean equals(final Object o) {
		if (parameter == null | o == null) {
			return parameter == o;
		} else {
			final Function f = (Function) o;
			if (f instanceof Expression) {
				return getParameter(0).equals(((Expression) f).getParameter(0));
			} else {
				return getParameter(0).equals(f);
			}
		}
	}

	@Override
	public Expression clone() {
		return new Expression(mathContext, parameter);
	}

}
