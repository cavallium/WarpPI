package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;

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
	public void render() {
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3i(255, 0, 0);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
	}

	@Override
	public void beforeRender(final float dt) {

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
