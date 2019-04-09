package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
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
	public void graphicInitialized() throws InterruptedException {}

	@Override
	public void render() {
		DisplayOutputDevice display = d.display;
		Utils.getFont(false, true).use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2 + 1, display.getDisplaySize()[1] / 2 - 20, "WORK IN PROGRESS.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, display.getDisplaySize()[1] / 2 - 20 + 1, "WORK IN PROGRESS.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2 + 1, display.getDisplaySize()[1] / 2 - 20 + 1, "WORK IN PROGRESS.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3i(255, 0, 0);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, display.getDisplaySize()[1] / 2 - 20, "WORK IN PROGRESS.");

		Utils.getFont(false, false).use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2 + 1, display.getDisplaySize()[1] / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, display.getDisplaySize()[1] / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2 + 1, display.getDisplaySize()[1] / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3i(255, 0, 0);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, display.getDisplaySize()[1] / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
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
