package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;

public class SolveForXScreen extends Screen {

	@SuppressWarnings("unused")
	private final MathInputScreen es;

	public SolveForXScreen(final MathInputScreen es) {
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
		DisplayOutputDevice display = d.display;
		ctx.getRenderer().glColor4i(0, 0, 0, 64);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2 + 1, ctx.getHeight() / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		ctx.getRenderer().glColor3i(255, 0, 0);
		ctx.getRenderer().glDrawStringCenter(ctx.getWidth() / 2, ctx.getHeight() / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
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
				//TODO: far funzionare questa parte
				/*Engine.INSTANCE.getHardwareDevice().getDisplayManager().goBack();
				try {
					es.calc.solveExpression('X');
				} catch (final Error e) {
					final Screen scr = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
					if (scr instanceof MathInputScreen) {
						final MathInputScreen escr = (MathInputScreen) scr;
						escr.errorLevel = 1;
						//escr.err2 = e; //TODO: What is this variable, and why it doesn't exists?
					} else {
						e.printStackTrace();
					}
				}
				*/
				return true;
			default:
				return false;
		}
	}

	@Override
	public String getSessionTitle() {
		return "Solve for X";
	}

}
