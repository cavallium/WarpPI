package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Variable.VariableValue;
import it.cavallium.warppi.util.Utils;

public class ChooseVariableValueScreen extends Screen {

	@SuppressWarnings("unused")
	private final MathInputScreen es;
	public Function resultNumberValue;

	public ChooseVariableValueScreen(final MathInputScreen es, final VariableValue variableValue) {
		super();
		historyBehavior = HistoryBehavior.DONT_KEEP_IN_HISTORY;

		this.es = es;
	}

	@Override
	public void created() throws InterruptedException {}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void graphicInitialized(ScreenContext ctx) throws InterruptedException {}

	@Override
	public void render(RenderContext ctx) {
		Utils.getFont(false, true).use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		ctx.getRenderer().glColor4i(0, 0, 0, 64);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 2 - 20, "WORK IN PROGRESS.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 2 - 20 + 1, "WORK IN PROGRESS.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 2 - 20 + 1, "WORK IN PROGRESS.");
		ctx.getRenderer().glColor3i(255, 0, 0);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 2 - 20, "WORK IN PROGRESS.");

		Utils.getFont(false, false).use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		ctx.getRenderer().glColor4i(0, 0, 0, 64);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glColor3i(255, 0, 0);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
	}

	@Override
	public void beforeRender(ScreenContext ctx, final float dt) {

	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent k) {
		switch (k.getKey()) {
			case LETTER_X:
//				PIDisplay.INSTANCE.goBack();
//				try {
//					Calculator.solveExpression('X');
//				} catch (Error e) {
//					Screen scr = PIDisplay.INSTANCE.getScreen();
//					if (scr instanceof MathInputScreen) {
//						MathInputScreen escr = (MathInputScreen) scr;
//						escr.errorLevel = 1;
//						escr.err2 = e;
//					} else {
//						e.printStackTrace();
//					}
//				}
				return true;
			default:
				return false;
		}
	}

	@Override
	public String getSessionTitle() {
		return "Choose a value for the variable.";
	}

}
