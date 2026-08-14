package it.cavallium.warppi.gui.screens;

import java.io.IOException;

import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockFixedPower;
import it.cavallium.warppi.gui.expression.blocks.BlockLogarithm;
import it.cavallium.warppi.gui.expression.blocks.BlockMixedFraction;
import it.cavallium.warppi.gui.expression.blocks.BlockNegative;
import it.cavallium.warppi.gui.expression.blocks.BlockPostfixFunction;
import it.cavallium.warppi.gui.expression.blocks.BlockPower;
import it.cavallium.warppi.gui.expression.blocks.BlockRandomInteger;
import it.cavallium.warppi.gui.expression.blocks.BlockScientificFunction;
import it.cavallium.warppi.gui.expression.blocks.BlockVariable;
import it.cavallium.warppi.gui.expression.containers.ExpressionInputDiagnostic;
import it.cavallium.warppi.gui.expression.containers.InputContainer;
import it.cavallium.warppi.gui.expression.containers.NormalInputContainer;
import it.cavallium.warppi.gui.expression.containers.NormalOutputContainer;
import it.cavallium.warppi.gui.expression.containers.OutputContainer;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.math.AngleMode;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.VariableValue;
import it.cavallium.warppi.math.parser.MathParser;
import it.cavallium.warppi.math.solver.MathSolver;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MathInputScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFFC5C2AF;
	private static final int ERROR_BACKGROUND_COLOR = 0xFFDC3C32;
	private static final int INPUT_ERROR_COLOR = 0xFFB3261E;
	private static final int INPUT_ERROR_PANEL_COLOR = 0xFFFFE9E6;
	private static final int INPUT_ERROR_TEXT_COLOR = 0xFF4A1712;
	private static final int INPUT_ERROR_HINT_COLOR = 0xFF7A3A34;
	private static final int VIEWPORT_INDICATOR_COLOR = 0xFF5D5B51;
	private static final int VIEWPORT_TRACK_COLOR = 0x405D5B51;
	private static final int PADDING = 4;
	private static final int INPUT_VIEWPORT_MAX_HEIGHT = 112;
	private static final int VIEWPORT_GAP = 6;
	private static final int PAN_STEP = 32;
	private static final int VIEWPORT_EDGE_GUTTER = 3;

	private static final BinaryFont fontBig = Utils.getFont(false);
	private final boolean isCloned;

	public MathContext calc;
	public InputContext ic;
	public InputContainer userInput;
	public OutputContainer result;
	public int errorLevel = 0; // 0 = nessuno, 1 = risultato, 2 = tutto
	private volatile boolean computingResult = false;
	private Thread computingThread;
	private int computingAnimationIndex = 0;
	private double computingAnimationElapsedTime = 0;
	private double computingElapsedTime = 0;
	private boolean computingBreakTipVisible = false;
	private volatile InputFeedback inputFeedback;
	private volatile Function answer;
	private int inputScrollX;
	private int inputScrollY;
	private int resultScrollX;
	private int resultScrollY;
	private int inputViewportWidth;
	private int inputViewportHeight;
	private int inputContentWidth;
	private int inputContentHeight;
	private int resultViewportWidth;
	private int resultViewportHeight;
	private boolean resultNavigationActive;
	volatile boolean mustRefresh = true;
	@SuppressWarnings("unused")
	private int currentStep = 0;

	private record InputFeedback(String title, String message, String correction) {}

	public MathInputScreen() {
		super();
		historyBehavior = HistoryBehavior.NORMAL;
		isCloned = false;
	}

	/**
	 * Create a copy of this element
	 */
	private MathInputScreen(MathInputScreen old) {
		this.calc = new MathContext(old.calc);
		this.historyBehavior = old.historyBehavior;
		this.created = old.created;
		this.currentStep = old.currentStep;
		this.d = old.d;
		this.errorLevel = old.errorLevel;
		this.ic = new InputContext(old.ic);
		this.initialized = old.initialized;
		this.mustRefresh = old.mustRefresh;
		this.result = new NormalOutputContainer(old.result);
		this.userInput = new NormalInputContainer(old.userInput, this.ic);
		this.inputFeedback = old.inputFeedback;
		this.answer = old.answer == null ? null : old.answer.clone(this.calc);
		this.inputScrollX = old.inputScrollX;
		this.inputScrollY = old.inputScrollY;
		this.resultScrollX = old.resultScrollX;
		this.resultScrollY = old.resultScrollY;
		this.inputContentWidth = old.inputContentWidth;
		this.inputContentHeight = old.inputContentHeight;
		this.resultNavigationActive = old.resultNavigationActive;
		this.isCloned = true;
	}

	@Override
	public void created() throws InterruptedException {
		ic = new InputContext();
		calc = new MathContext();

		calc.init();
	}

	@Override
	public void initialized() throws InterruptedException {
		/* Fine caricamento */
	}

	@Override
	public void graphicInitialized(ScreenContext ctx) throws InterruptedException {
		/* Fine caricamento */
		try {
			if (!BlockContainer.isInitialized()) {
				BlockContainer.initializeFonts(ctx.getGraphicEngine().loadFont("norm"), ctx.getGraphicEngine().loadFont("smal"));
			}
		} catch (final IOException e) {
			e.printStackTrace();
			WarpPI.getPlatform().exit(1);
		}

		if (!isCloned) {
			userInput = new NormalInputContainer(ic);
			result = new NormalOutputContainer();
		}
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
		if (userInput != null)
			if (userInput.beforeRender(dt)) {
				mustRefresh = true;
			}
		if (computingResult) {
			computingElapsedTime += dt;
			computingAnimationElapsedTime += dt;
			if (computingAnimationElapsedTime > 0.1) {
				computingAnimationElapsedTime -= 0.1;
				computingAnimationIndex = (computingAnimationIndex + 1) % 16;
				mustRefresh = true;
			}
			if (computingElapsedTime > 5) {
				computingBreakTipVisible = true;
			}
		} else {
			computingElapsedTime = 0;
			computingAnimationElapsedTime = 0;
			computingAnimationIndex = 0;
			computingBreakTipVisible = false;
		}
	}

	@Override
	public int getBackgroundColor() {
		return d != null && d.error != null ? ERROR_BACKGROUND_COLOR : BACKGROUND_COLOR;
	}

	@Override
	public void render(RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		final InputFeedback feedback = inputFeedback;
		MathInputScreen.fontBig.use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		final int textColor = 0xFF000000;
		renderer.glColor(textColor);

		drawInputViewport(ctx);

		if (computingResult) {
			renderer.glColor3f(1, 1, 1);
			final int leftX = 208;
			final int leftY = 16;
			final int size = 32;
			final int posY = computingAnimationIndex % 2;
			final int posX = (computingAnimationIndex - posY) / 2;
			renderer.glFillRect(ctx.getWidth() - size - 4, ctx.getHeight() - size - 4, size, size, leftX + size * posX, leftY + size * posY, size, size);
			if (computingBreakTipVisible) {
				Utils.getFont(false).use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
				renderer.glColor3f(0.75f, 0, 0);
				renderer.glDrawStringRight(ctx.getWidth() - 4 - size - 4, ctx.getHeight() - size / 2 - renderer.getCurrentFont().getCharacterHeight() / 2 - 4, "Press (=) to stop");
			}
		} else if (feedback != null) {
			drawInputFeedback(ctx, feedback);
		} else if (!result.isContentEmpty()) {
			drawResultViewport(ctx);
		}
	}

	private void drawInputViewport(final RenderContext ctx) {
		inputViewportWidth = Math.max(1, ctx.getWidth() - PADDING * 2 - 2);
		final int availableHeight = Math.max(1, ctx.getHeight() - PADDING * 2);
		final int reservedResultHeight = !result.isContentEmpty() && inputFeedback == null && !computingResult
				? Math.min(80, Math.max(32, availableHeight / 3)) : 0;
		final int inputHeightLimit = Math.max(1,
				Math.min(INPUT_VIEWPORT_MAX_HEIGHT, availableHeight - reservedResultHeight - VIEWPORT_GAP));
		inputViewportHeight = Math.min(inputHeightLimit, Math.max(userInput.getHeight(), 1));
		followInputCaret(inputViewportWidth, inputViewportHeight);
		final Renderer viewport = ctx.getRenderer().getClippedInstance(
				PADDING, PADDING, inputViewportWidth, inputViewportHeight);
		userInput.draw(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display,
				viewport, -inputScrollX, -inputScrollY);
		drawScrollIndicators(ctx.getRenderer(), PADDING, PADDING,
				inputViewportWidth, inputViewportHeight, inputContentWidth, inputContentHeight,
				inputScrollX, inputScrollY, false);
	}

	private void drawResultViewport(final RenderContext ctx) {
		final int top = Math.min(ctx.getHeight() - 1,
				PADDING + inputViewportHeight + VIEWPORT_GAP);
		resultViewportWidth = Math.max(1, ctx.getWidth() - PADDING * 2 - 2);
		resultViewportHeight = Math.max(1, ctx.getHeight() - top - PADDING - 2);
		clampResultScroll();
		final Renderer viewport = ctx.getRenderer().getClippedInstance(
				PADDING, top, resultViewportWidth, resultViewportHeight);
		result.draw(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display,
				viewport, -resultScrollX, -resultScrollY);
		final boolean oversized = isResultOversized();
		drawScrollIndicators(ctx.getRenderer(), PADDING, top,
				resultViewportWidth, resultViewportHeight,
				resultScrollableWidth(), resultScrollableHeight(),
				resultScrollX, resultScrollY, resultNavigationActive && oversized);
	}

	private void followInputCaret(final int viewportWidth, final int viewportHeight) {
		final InputContainer.CaretBounds bounds = userInput.measureCaret(
				WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		inputContentWidth = Math.max(userInput.getWidth() + VIEWPORT_EDGE_GUTTER,
				bounds.x() + bounds.width() + VIEWPORT_EDGE_GUTTER);
		inputContentHeight = Math.max(userInput.getHeight(),
				bounds.y() + bounds.height());
		inputScrollX = reveal(bounds.x(), bounds.x() + bounds.width(), inputScrollX,
				viewportWidth, inputContentWidth);
		inputScrollY = reveal(bounds.y(), bounds.y() + bounds.height(), inputScrollY,
				viewportHeight, inputContentHeight);
	}

	private static int reveal(final int start, final int end, final int currentOffset,
			final int viewportSize, final int contentSize) {
		final int margin = Math.min(12, Math.max(2, viewportSize / 8));
		int offset = currentOffset;
		if (start - margin < offset) {
			offset = start - margin;
		} else if (end + margin > offset + viewportSize) {
			offset = end + margin - viewportSize;
		}
		return clamp(offset, 0, Math.max(0, contentSize - viewportSize));
	}

	private void drawScrollIndicators(final Renderer renderer, final int x, final int y,
			final int viewportWidth, final int viewportHeight, final int contentWidth,
			final int contentHeight, final int scrollX, final int scrollY, final boolean active) {
		final int color = active ? INPUT_ERROR_COLOR : VIEWPORT_INDICATOR_COLOR;
		if (contentWidth > viewportWidth) {
			final int trackWidth = Math.max(1, viewportWidth - 4);
			final int thumbWidth = Math.min(trackWidth,
					Math.max(10, (int) ((long) trackWidth * viewportWidth / contentWidth)));
			final int travel = Math.max(0, trackWidth - thumbWidth);
			final int thumbX = x + 2 + (int) ((long) travel * scrollX
					/ Math.max(1, contentWidth - viewportWidth));
			renderer.glColor(VIEWPORT_TRACK_COLOR);
			renderer.glFillColor(x + 2, y + viewportHeight, trackWidth, 2);
			renderer.glColor(color);
			renderer.glFillColor(thumbX, y + viewportHeight, thumbWidth, 2);
		}
		if (contentHeight > viewportHeight) {
			final int trackHeight = Math.max(1, viewportHeight - 4);
			final int thumbHeight = Math.min(trackHeight,
					Math.max(10, (int) ((long) trackHeight * viewportHeight / contentHeight)));
			final int travel = Math.max(0, trackHeight - thumbHeight);
			final int thumbY = y + 2 + (int) ((long) travel * scrollY
					/ Math.max(1, contentHeight - viewportHeight));
			renderer.glColor(VIEWPORT_TRACK_COLOR);
			renderer.glFillColor(x + viewportWidth, y + 2, 2, trackHeight);
			renderer.glColor(color);
			renderer.glFillColor(x + viewportWidth, thumbY, 2, thumbHeight);
		}
	}

	private boolean isResultOversized() {
		return !result.isContentEmpty() && resultViewportWidth > 0 && resultViewportHeight > 0
				&& (resultScrollableWidth() > resultViewportWidth
						|| resultScrollableHeight() > resultViewportHeight);
	}

	private void clampResultScroll() {
		resultScrollX = clamp(resultScrollX, 0,
				Math.max(0, resultScrollableWidth() - resultViewportWidth));
		resultScrollY = clamp(resultScrollY, 0,
				Math.max(0, resultScrollableHeight() - resultViewportHeight));
	}

	private int resultScrollableWidth() {
		return result.isContentEmpty() ? 0 : result.getWidth() + VIEWPORT_EDGE_GUTTER;
	}

	private int resultScrollableHeight() {
		return result.getHeight();
	}

	private static int clamp(final int value, final int minimum, final int maximum) {
		return Math.max(minimum, Math.min(value, maximum));
	}

	private void drawInputFeedback(final RenderContext ctx, final InputFeedback feedback) {
		final Renderer renderer = ctx.getRenderer();
		final BinaryFont font = Utils.getFont(true, false);
		font.use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		final int padding = 4;
		final int panelX = 4;
		final int panelWidth = Math.max(1, ctx.getWidth() - panelX * 2);
		final int lineHeight = font.getCharacterHeight();
		final int panelHeight = lineHeight * 3 + padding * 4;
		final int desiredY = PADDING + inputViewportHeight + VIEWPORT_GAP;
		final int panelY = Math.max(4, Math.min(desiredY, ctx.getHeight() - panelHeight - 4));

		renderer.glColor(INPUT_ERROR_PANEL_COLOR);
		renderer.glFillColor(panelX, panelY, panelWidth, panelHeight);
		renderer.glColor(INPUT_ERROR_COLOR);
		renderer.glFillColor(panelX, panelY, 3, panelHeight);
		renderer.glDrawLine(panelX, panelY, panelX + panelWidth - 1, panelY);
		renderer.glDrawLine(panelX, panelY + panelHeight - 1, panelX + panelWidth - 1, panelY + panelHeight - 1);
		renderer.glDrawLine(panelX + panelWidth - 1, panelY, panelX + panelWidth - 1, panelY + panelHeight - 1);
		renderer.glDrawStringLeft(panelX + 7, panelY + padding, feedback.title());
		renderer.glColor(INPUT_ERROR_TEXT_COLOR);
		renderer.glDrawStringLeft(panelX + 7, panelY + padding * 2 + lineHeight,
				fitToWidth(font, feedback.message(), panelWidth - 11));
		renderer.glColor(INPUT_ERROR_HINT_COLOR);
		renderer.glDrawStringLeft(panelX + 7, panelY + padding * 3 + lineHeight * 2,
				fitToWidth(font, "FIX: " + feedback.correction(), panelWidth - 11));
	}

	private static String fitToWidth(final BinaryFont font, final String text, final int maxWidth) {
		if (font.getStringWidth(text) <= maxWidth) {
			return text;
		}
		final String suffix = "...";
		int end = text.length();
		while (end > 0 && font.getStringWidth(text.substring(0, end) + suffix) > maxWidth) {
			end--;
		}
		return end == 0 ? suffix : text.substring(0, end) + suffix;
	}

	@Override
	public void renderTopmost(RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		renderer.glColor3f(1, 1, 1);
		final int pos = 2;
		final int spacersNumb = 1;
		int skinN = 0;
		if (calc.exactMode) {
			skinN = 22;
		} else {
			skinN = 21;
		}
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().guiSkin.use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		renderer.glFillRect(2 + 18 * pos + 2 * spacersNumb, 2, 16, 16, 16 * skinN, 16 * 0, 16, 16);
	}

	@Override
	public boolean mustBeRefreshed() {
		if (mustRefresh) {
			mustRefresh = false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent k) {
		WarpPI.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "MathInputScreen", "Pressed key " + k.getKey().toString());
		try {
			if (resultNavigationActive && handleResultNavigation(k.getKey())) {
				return true;
			}
			switch (k.getKey()) {
				case OK:
					clearInputFeedback();
					userInput.toggleExtra();
					mustRefresh = true;
					return true;
				case HISTORY_BACK:
					if (userInput.isExtraOpened()) {
						userInput.closeExtra();
						currentStep = 0;
						mustRefresh = true;
						return true;
					}
				case BACK:
					if (userInput.isExtraOpened()) {
						userInput.closeExtra();
						mustRefresh = true;
						return true;
					}
				default:
					if (userInput.isExtraOpened() && userInput.getExtraKeyboardEventListener().onKeyPressed(k)) {
						currentStep = 0;
						return true;
					} else {
						final boolean step = k.getKey() == Key.STEP;
						switch (k.getKey()) {

							case STEP:
								currentStep++;
								return simplify(true, false);
							case SIMPLIFY:
								return simplify(false, true);
							case NUM0:
								typeChar('0');
								return true;
							case NUM1:
								typeChar('1');
								return true;
							case NUM2:
								typeChar('2');
								return true;
							case NUM3:
								typeChar('3');
								return true;
							case NUM4:
								typeChar('4');
								return true;
							case NUM5:
								typeChar('5');
								return true;
							case NUM6:
								typeChar('6');
								return true;
							case NUM7:
								typeChar('7');
								return true;
							case NUM8:
								typeChar('8');
								return true;
							case NUM9:
								typeChar('9');
								return true;
							case PLUS:
								typeChar('+');
								return true;
							case MINUS:
								typeChar('-');
								return true;
							case PLUS_MINUS:
								typeChar('±');
								return true;
							case MULTIPLY:
								typeChar('*');
								return true;
							case DIVIDE:
								typeChar('/');
								return true;
							case FRACTION:
								typeChar(MathematicalSymbols.DIVISION);
								return true;
							case MIXED_FRACTION:
								if (!result.isContentEmpty()) {
									calc.mixedFractionMode = !calc.mixedFractionMode;
									rerenderAnswer();
								} else {
									typeMixedFraction();
								}
								return true;
							case NEGATIVE:
								typeNegative();
								return true;
							case PARENTHESIS_OPEN:
								typeChar('(');
								return true;
							case PARENTHESIS_CLOSE:
								typeChar(')');
								return true;
							case DOT:
								typeChar('.');
								return true;
							case EQUAL:
								typeChar('=');
								return true;
							case SQRT:
								typeChar('Ⓐ');
								return true;
							case ROOT:
								typeChar('√');
								return true;
							case POWER_OF_2:
								typeChar(MathematicalSymbols.POWER_OF_TWO);
								return true;
							case POWER_OF_x:
								typeChar(MathematicalSymbols.POWER);
								return true;
							case POWER_OF_3:
								typeBlock(new BlockFixedPower("3"));
								return true;
							case RECIPROCAL:
								typeBlock(new BlockFixedPower("-1"));
								return true;
							case POWER_OF_10:
								typePowerWithBase("10");
								return true;
							case POWER_OF_E:
								typePowerWithBase(MathematicalSymbols.EULER_NUMBER);
								return true;
							case EXPONENTIAL_NOTATION:
								typeChar(MathematicalSymbols.MULTIPLICATION);
								typePowerWithBase("10");
								return true;
							case PI:
								typeChar(MathematicalSymbols.PI);
								return true;
							case EULER_NUMBER:
								typeChar(MathematicalSymbols.EULER_NUMBER);
								return true;
							case LETTER_X:
								typeChar(MathematicalSymbols.variables[23]);
								return true;
							case LETTER_Y:
								typeChar(MathematicalSymbols.variables[24]);
								return true;
							case LETTER_A, LETTER_B, LETTER_C, LETTER_D, LETTER_E, LETTER_F,
									LETTER_G, LETTER_H, LETTER_I, LETTER_J, LETTER_K, LETTER_L,
									LETTER_M, LETTER_N, LETTER_O, LETTER_P, LETTER_Q, LETTER_R,
									LETTER_S, LETTER_T, LETTER_U, LETTER_V, LETTER_W, LETTER_Z:
								typeChar(letterFor(k.getKey()));
								return true;
							case SINE:
								typeChar(MathematicalSymbols.SINE);
								return true;
							case COSINE:
								typeChar(MathematicalSymbols.COSINE);
								return true;
							case TANGENT:
								typeChar(MathematicalSymbols.TANGENT);
								return true;
							case ARCSINE:
								typeChar(MathematicalSymbols.ARC_SINE);
								return true;
							case ARCCOSINE:
								typeChar(MathematicalSymbols.ARC_COSINE);
								return true;
							case ARCTANGENT:
								typeChar(MathematicalSymbols.ARC_TANGENT);
								return true;
							case LOGARITHM:
								typeChar(MathematicalSymbols.LOGARITHM);
								return true;
							case COMMON_LOGARITHM:
								typeBlock(new BlockLogarithm('1', '0'));
								return true;
							case NATURAL_LOGARITHM:
								typeLogarithmWithEulerBase();
								return true;
							case ABSOLUTE_VALUE:
								typeScientificFunction(ScientificFunctionKind.ABSOLUTE);
								return true;
							case ROUND:
								typeScientificFunction(ScientificFunctionKind.ROUND);
								return true;
							case FACTORIZE:
								typeScientificFunction(ScientificFunctionKind.FACTORIZE);
								return true;
							case FACTORIAL:
								typePostfixFunction(ScientificFunctionKind.FACTORIAL);
								return true;
							case PERCENT:
								typePostfixFunction(ScientificFunctionKind.PERCENT);
								return true;
							case DEGREE_MINUTE_SECOND:
								typePostfixFunction(ScientificFunctionKind.DEGREE_MINUTE_SECOND);
								return true;
							case ANSWER:
								return typeAnswer();
							case LESS_THAN:
								typeChar(MathematicalSymbols.LESS_THAN);
								return true;
							case LESS_OR_EQUAL:
								typeChar(MathematicalSymbols.LESS_OR_EQUAL);
								return true;
							case GREATER_THAN:
								typeChar(MathematicalSymbols.GREATER_THAN);
								return true;
							case GREATER_OR_EQUAL:
								typeChar(MathematicalSymbols.GREATER_OR_EQUAL);
								return true;
							case RANDOM_INTEGER:
								typeRandomIntegerTemplate();
								return true;
							case DELETE:
								clearInputFeedback();
								userInput.del();
								currentStep = 0;
								mustRefresh = true;
								return true;
							case LEFT:
								clearInputFeedback();
								userInput.moveLeft();
								mustRefresh = true;
								return true;
							case RIGHT:
								clearInputFeedback();
								userInput.moveRight();
								mustRefresh = true;
								return true;
							case UP:
								clearInputFeedback();
								userInput.moveUp(WarpPI.INSTANCE.getHardwareDevice()
										.getDisplayManager().display);
								mustRefresh = true;
								return true;
							case DOWN:
								clearInputFeedback();
								if (!userInput.moveDown(WarpPI.INSTANCE.getHardwareDevice()
										.getDisplayManager().display) && isResultOversized()) {
									resultNavigationActive = true;
									clampResultScroll();
								}
								mustRefresh = true;
								return true;
							case RESET:
								clearInputFeedback();
								userInput.clear();
								result.clear();
								resetViewports();
								currentStep = 0;
								if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error != null) {
									WarpPI.getPlatform().getConsoleUtils().out().println(1, "Resetting after error...");
									WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error = null;
								}
								return true;
							case SURD_MODE:
								calc.exactMode = !calc.exactMode;
								if (!result.isContentEmpty()) {
									result.clear();
									currentStep = 0;
									simplify(false, false);
								}
								return true;
							case debug1:
								WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new EmptyScreen());
								return true;
							case HISTORY_BACK:
								//					if (Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoBack()) {
								//						if (currentExpression != null && currentExpression.length() > 0 & Engine.INSTANCE.getHardwareDevice().getDisplayManager().sessions[Engine.INSTANCE.getHardwareDevice().getDisplayManager().currentSession + 1] instanceof MathInputScreen) {
								//							newExpression = currentExpression;
								//							try {
								//								interpreta(true);
								//							} catch (final Error e) {}
								//						}
								//					}
								return false;
							case HISTORY_FORWARD:
								//					if (Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoForward()) {
								//						if (currentExpression != null && currentExpression.length() > 0 & Engine.INSTANCE.getHardwareDevice().getDisplayManager().sessions[Engine.INSTANCE.getHardwareDevice().getDisplayManager().currentSession - 1] instanceof MathInputScreen) {
								//							newExpression = currentExpression;
								//							try {
								//								interpreta(true);
								//							} catch (final Error e) {}
								//						}
								//					}
								return false;
							case debug_DEG:
								if (calc.angleMode.equals(AngleMode.DEG) == false) {
									calc.angleMode = AngleMode.DEG;
									currentStep = 0;
									return true;
								}
								return false;
							case debug_RAD:
								if (calc.angleMode.equals(AngleMode.RAD) == false) {
									calc.angleMode = AngleMode.RAD;
									currentStep = 0;
									return true;
								}
								return false;
							case debug_GRA:
								if (calc.angleMode.equals(AngleMode.GRA) == false) {
									calc.angleMode = AngleMode.GRA;
									currentStep = 0;
									return true;
								}
								return false;
							case DRG_CYCLE:
								if (calc.angleMode.equals(AngleMode.DEG) == true) {
									calc.angleMode = AngleMode.RAD;
								} else if (calc.angleMode.equals(AngleMode.RAD) == true) {
									calc.angleMode = AngleMode.GRA;
								} else {
									calc.angleMode = AngleMode.DEG;
								}
								currentStep = 0;
								return true;
							default:
								return false;
						}
					}
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			return true;
		}
	}

	private boolean handleResultNavigation(final Key key) {
		switch (key) {
			case LEFT -> resultScrollX -= PAN_STEP;
			case RIGHT -> resultScrollX += PAN_STEP;
			case UP -> resultScrollY -= PAN_STEP;
			case DOWN -> resultScrollY += PAN_STEP;
			case OK, BACK -> {
				resultNavigationActive = false;
				mustRefresh = true;
				return true;
			}
			default -> {
				resultNavigationActive = false;
				return false;
			}
		}
		clampResultScroll();
		mustRefresh = true;
		return true;
	}

	private void resetViewports() {
		inputScrollX = 0;
		inputScrollY = 0;
		resultScrollX = 0;
		resultScrollY = 0;
		resultNavigationActive = false;
	}

	@SuppressWarnings("unchecked")
	private void swapInputScreen() {
		MathInputScreen mis = new MathInputScreen(this);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(mis);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private ObjectArrayList<Function> solveExpression(final ObjectArrayList<Function> f22) {
		return null;
		/*
		try {
			try {
				return calc.solveExpression(f22);
			} catch (final Exception ex) {
				if (Utils.debugOn) {
					ex.printStackTrace();
				}
				throw new Error(Errors.SYNTAX_ERROR);
			}
		} catch (final Error e) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			d.errorStackTrace = sw.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().error = e.id.toString();
			System.err.println(e.id);
		}
		return null;
		*/
	}

	@Deprecated
	protected void step() {
		/*
		try {
			try {
				showVariablesDialog();
				ObjectArrayList<Function> results = new ObjectArrayList<>();
				final ObjectArrayList<Function> partialResults = new ObjectArrayList<>();
				for (final Function f : calc.f2) {
					if (f instanceof Equation) {
						Engine.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new SolveEquationScreen(this));
					} else {
						results.add(f);
						for (final Function itm : results) {
							if (itm.isSimplified() == false) {
								final List<Function> dt = itm.simplify();
								partialResults.addAll(dt);
							} else {
								partialResults.add(itm);
							}
						}
						results = new ObjectArrayList<>(partialResults);
						partialResults.clear();
					}
				}

				if (results.size() == 0) {
					calc.resultsCount = 0;
				} else {
					calc.resultsCount = results.size();
					Collections.reverse(results);
					// add elements to al, including duplicates
					final Set<Function> hs = new LinkedHashSet<>();
					hs.addAll(results);
					results.clear();
					results.addAll(hs);
					calc.f2 = results;
				}
				Utils.out.println(1, calc.f2.toString());
			} catch (final Exception ex) {
				if (Utils.debugOn) {
					ex.printStackTrace();
				}
				throw new Error(Errors.SYNTAX_ERROR);
			}
		} catch (final Error e) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			d.errorStackTrace = sw.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().error = e.id.toString();
			System.err.println(e.id);
		}
		*/
	}

	protected boolean simplify(final boolean step, final boolean swapScreen) {
		if (!step) {
			currentStep = 0;
		}
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error != null) {
			//TODO: make the error management a global API rather than being relegated to this screen.
			WarpPI.getPlatform().getConsoleUtils().out().println(1, "Resetting after error...");
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error = null;
			calc.f = null;
			calc.f2 = null;
			calc.resultsCount = 0;
			return true;
		} else if (!computingResult) {
			clearInputFeedback();
			computingResult = true;
			computingThread = new Thread(() -> {
				try {
					computeResult(swapScreen);
				} catch (final InterruptedException ex) {
					WarpPI.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN,
							"Computing thread stopped.");
				} finally {
					computingResult = false;
					WarpPI.INSTANCE.endLoadingOperation();
					mustRefresh = true;
				}
			});
			WarpPI.getPlatform().setThreadName(computingThread, "Computing Thread");
			WarpPI.getPlatform().setThreadDaemon(computingThread);
			computingThread.setPriority(Thread.NORM_PRIORITY + 3);
			WarpPI.INSTANCE.beginLoadingOperation();
			try {
				computingThread.start();
			} catch (final RuntimeException | java.lang.Error failure) {
				computingResult = false;
				WarpPI.INSTANCE.endLoadingOperation();
				throw failure;
			}
			return true;
		} else {
			if (computingThread != null) {
				computingThread.interrupt();
				computingResult = false;
				return true;
			}
			return false;
		}
	}

	private void computeResult(final boolean swapScreen) throws InterruptedException {
		if (userInput.isAlreadyParsed()) {
			return;
		}
		if (userInput.isEmpty()) {
			showSyntaxFeedback(null);
			return;
		}

		final Expression expression;
		try {
			expression = MathParser.parseInput(calc, userInput);
		} catch (final Error | RuntimeException failure) {
			showSyntaxFeedback(failure);
			logComputationFailure(failure);
			return;
		}

		try {
			if (calc.f == null || calc.f2 == null) {
				calc.f = new ObjectArrayList<>();
				calc.f2 = new ObjectArrayList<>();
			} else {
				calc.f.clear();
				calc.f2.clear();
			}
			calc.f.add(expression);
			WarpPI.getPlatform().getConsoleUtils().out().println(2, "INPUT: " + expression);
			final MathSolver solver = new MathSolver(expression);
			final ObjectArrayList<ObjectArrayList<Function>> resultSteps = solver.solveAllSteps();
			resultSteps.add(0, Utils.newArrayList(expression));
			int stepNumber = 0;
			for (final ObjectArrayList<Function> resultStep : resultSteps) {
				stepNumber++;
				WarpPI.getPlatform().getConsoleUtils().out().println(0, "STEP " + stepNumber);
				for (final Function function : resultStep) {
					WarpPI.getPlatform().getConsoleUtils().out().println(0, " :: " + function);
				}
			}
			final ObjectArrayList<Function> resultExpressions = resultSteps.get(resultSteps.size() - 1);
			if (!resultExpressions.isEmpty())
				answer = resultExpressions.getFirst().clone(calc);
			for (final Function resultExpression : resultExpressions) {
				WarpPI.getPlatform().getConsoleUtils().out().println(0, "RESULT: " + resultExpression);
			}
			final ObjectArrayList<ObjectArrayList<Block>> resultBlocks = MathParser.parseOutput(calc,
					resultExpressions);
			result.setContentAsMultipleGroups(resultBlocks);
			resultScrollX = 0;
			resultScrollY = 0;
			resultNavigationActive = false;
			if (swapScreen) {
				swapInputScreen();
			}
		} catch (final Error failure) {
			showCalculationFeedback(calculationMessage(failure), calculationCorrection(failure));
			logComputationFailure(failure);
		} catch (final RuntimeException failure) {
			showCalculationFeedback("COULD NOT EVALUATE THIS EXPRESSION",
					"EDIT THE EXPRESSION AND TRY AGAIN");
			logComputationFailure(failure);
		}
	}

	private void showSyntaxFeedback(final Exception failure) {
		resultNavigationActive = false;
		ExpressionInputDiagnostic diagnostic = userInput.diagnoseSyntaxError();
		if (diagnostic == null) {
			diagnostic = new ExpressionInputDiagnostic(userInput.getCaretPosition(),
					ExpressionInputDiagnostic.HighlightTarget.INSERTION_POINT, "CHECK INPUT",
					fallbackSyntaxMessage(failure), "EDIT AT THE CURSOR AND TRY AGAIN");
		}
		userInput.showError(diagnostic);
		inputFeedback = new InputFeedback(diagnostic.title(), diagnostic.message(), diagnostic.correction());
	}

	private void showCalculationFeedback(final String message, final String correction) {
		resultNavigationActive = false;
		userInput.clearErrorHighlight();
		inputFeedback = new InputFeedback("CALCULATION ERROR", message, correction);
	}

	private void clearInputFeedback() {
		if (userInput != null) {
			userInput.clearErrorHighlight();
		}
		inputFeedback = null;
		mustRefresh = true;
	}

	private static String fallbackSyntaxMessage(final Exception failure) {
		if (failure instanceof Error error && error.id == Errors.UNBALANCED_STACK) {
			return "CHECK THE ORDER OF THE OPERATORS";
		}
		if (failure instanceof Error error && error.id == Errors.MISSING_ARGUMENTS) {
			return "COMPLETE THE HIGHLIGHTED PART";
		}
		return "CHECK THE HIGHLIGHTED PART";
	}

	private static String calculationMessage(final Error failure) {
		return switch (failure.id) {
			case DIVISION_BY_ZERO -> "DIVISION BY ZERO IS NOT DEFINED";
			case NEGATIVE_PARAMETER -> "THE VALUE IS OUTSIDE THE FUNCTION DOMAIN";
			case NUMBER_TOO_LARGE -> "THE RESULT IS TOO LARGE";
			case NUMBER_TOO_SMALL -> "THE RESULT IS TOO SMALL";
			case TIMEOUT -> "THE CALCULATION TOOK TOO MANY STEPS";
			case NOT_AN_EQUATION -> "ENTER AN EQUATION TO SOLVE";
			default -> "COULD NOT EVALUATE THIS EXPRESSION";
		};
	}

	private static String calculationCorrection(final Error failure) {
		return switch (failure.id) {
			case DIVISION_BY_ZERO -> "CHANGE THE DENOMINATOR AND TRY AGAIN";
			case NEGATIVE_PARAMETER -> "CHANGE THE INPUT TO A VALID DOMAIN VALUE";
			case NUMBER_TOO_LARGE, NUMBER_TOO_SMALL -> "REDUCE THE INPUT RANGE AND TRY AGAIN";
			case TIMEOUT -> "SIMPLIFY THE EXPRESSION AND TRY AGAIN";
			case NOT_AN_EQUATION -> "ADD = TO ENTER AN EQUATION";
			default -> "EDIT THE EXPRESSION AND TRY AGAIN";
		};
	}

	private static void logComputationFailure(final Exception failure) {
		if (WarpPI.getPlatform().getSettings().isDebugEnabled()) {
			failure.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void changeEquationScreen() {
		throw new UnsupportedOperationException();
//
//		if (!userInput.isEmpty()) {
//			final MathInputScreen cloned = clone();
//			cloned.userInput.setCaretPosition(cloned.userInput.getCaretMaxPosition()-1);
//			Engine.INSTANCE.getHardwareDevice().getDisplayManager().replaceScreen(cloned);
//			initialized = false;
//			Engine.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(this);
//
//		}
	}

	public void typeChar(final char chr) {
		resultNavigationActive = false;
		clearInputFeedback();
		userInput.typeChar(chr);
		mustRefresh = true;
	}

	private void typeBlock(final Block block) {
		resultNavigationActive = false;
		clearInputFeedback();
		userInput.typeBlock(block);
		currentStep = 0;
		mustRefresh = true;
	}

	private void typePowerWithBase(final String base) {
		for (final char character : base.toCharArray())
			typeChar(character);
		typeBlock(new BlockPower());
	}

	private void typePowerWithBase(final char base) {
		typeChar(base);
		typeBlock(new BlockPower());
	}

	private void typeLogarithmWithEulerBase() {
		final BlockLogarithm logarithm = new BlockLogarithm();
		logarithm.getBaseContainer().appendBlock(new BlockVariable(
				userInput.getInputContext(), MathematicalSymbols.EULER_NUMBER, true));
		typeBlock(logarithm);
	}

	private void typeScientificFunction(final ScientificFunctionKind kind) {
		typeBlock(new BlockScientificFunction(kind));
	}

	private void typePostfixFunction(final ScientificFunctionKind kind) {
		typeBlock(new BlockPostfixFunction(kind));
	}

	private void typeRandomIntegerTemplate() {
		typeBlock(new BlockRandomInteger());
	}

	private void typeMixedFraction() {
		resultNavigationActive = false;
		clearInputFeedback();
		final BlockMixedFraction fraction = new BlockMixedFraction();
		if (!userInput.wrapExpressionToLeft(fraction, fraction.getWholeContainer(), 1))
			typeBlock(fraction);
		currentStep = 0;
		mustRefresh = true;
	}

	private void typeNegative() {
		resultNavigationActive = false;
		clearInputFeedback();
		final BlockNegative negative = new BlockNegative();
		if (!userInput.wrapExpressionToLeft(negative, negative.getNumberContainer(), 1))
			typeBlock(negative);
		currentStep = 0;
		mustRefresh = true;
	}

	private boolean typeAnswer() throws Error {
		final Function savedAnswer = answer;
		if (savedAnswer == null)
			return true;
		final MathContext inputContext = new MathContext(calc);
		inputContext.losslessInputMode = true;
		for (final Block block : savedAnswer.clone(inputContext).toBlock(inputContext))
			typeBlock(block);
		return true;
	}

	private void rerenderAnswer() throws Error {
		final Function savedAnswer = answer;
		if (savedAnswer == null)
			return;
		result.setContentAsMultipleGroups(MathParser.parseOutput(calc,
				new ObjectArrayList<>(new Function[] { savedAnswer.clone(calc) })));
		resultScrollX = 0;
		resultScrollY = 0;
		resultNavigationActive = false;
		mustRefresh = true;
	}

	private static char letterFor(final Key key) {
		return (char) ('A' + key.ordinal() - Key.LETTER_A.ordinal());
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent k) {
		if (k.getKey() == Key.OK) {
			return true;
		} else if (userInput.isExtraOpened() && userInput.getExtraKeyboardEventListener().onKeyReleased(k)) {
			return true;
		} else {
			switch (k.getKey()) {
				default:
					return false;
			}
		}
	}

	public void showVariablesDialog() {
		showVariablesDialog(null);
	}

	public void showVariablesDialog(final Runnable runnable) {
		final Thread ct = new Thread(() -> {
			final ObjectArrayList<Function> knownVarsInFunctions = getKnownVariables(calc.f.toArray(new Function[calc.f.size()]));
			for (final VariableValue f : calc.variablesValues) {
				if (knownVarsInFunctions.contains(f.v)) {
					knownVarsInFunctions.remove(f.v);
				}
			}

			boolean cancelled = false;
			for (final Function f : knownVarsInFunctions) {
				final ChooseVariableValueScreen cvs = new ChooseVariableValueScreen(this, new VariableValue((Variable) f, new Number(calc, 0)));
				WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(cvs);
				try {
					WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().screenChange.acquire();
				} catch (final InterruptedException e) {}
				if (cvs.resultNumberValue == null) {
					cancelled = true;
					break;
				} else {
					final int is = calc.variablesValues.size();
					for (int i = 0; i < is; i++) {
						if (calc.variablesValues.get(i).v == f) {
							calc.variablesValues.remove(i);
						}
					}
					calc.variablesValues.add(new VariableValue((Variable) f, (Number) cvs.resultNumberValue));
				}
			}
			if (!cancelled) {
				if (runnable != null) {
					runnable.run();
				}
			}
		});
		WarpPI.getPlatform().setThreadName(ct, "Variables user-input queue thread");
		ct.setPriority(Thread.MIN_PRIORITY);
		WarpPI.getPlatform().setThreadDaemon(ct);
		ct.start();
	}

	private ObjectArrayList<Function> getKnownVariables(final Function[] fncs) {
		final ObjectArrayList<Function> res = new ObjectArrayList<>();
		for (final Function f : fncs) {
			if (f instanceof FunctionOperator) {
				res.addAll(getKnownVariables(new Function[] { ((FunctionOperator) f).getParameter1(), ((FunctionOperator) f).getParameter2() }));
			} else if (f instanceof FunctionDynamic) {
				res.addAll(getKnownVariables(((FunctionDynamic) f).getParameters()));
			} else if (f instanceof FunctionSingle) {
				res.addAll(getKnownVariables(new Function[] { ((FunctionSingle) f).getParameter() }));
			} else if (f instanceof Variable) {
				if (((Variable) f).getType() == Variable.V_TYPE.CONSTANT) {
					if (!res.contains(f)) {
						res.add(f);
					}
				}
			}
		}
		return res;
	}

	@Override
	public String getSessionTitle() {
		return "Calculator";
	}

}
