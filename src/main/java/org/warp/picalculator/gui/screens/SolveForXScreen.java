package org.warp.picalculator.gui.screens;

import org.warp.picalculator.StaticVars;
import org.warp.picalculator.device.HardwareDevice;
import org.warp.picalculator.event.KeyPressedEvent;

public class SolveForXScreen extends Screen {

	@SuppressWarnings("unused")
	private final MathInputScreen es;

	public SolveForXScreen(MathInputScreen es) {
		super();
		canBeInHistory = false;

		this.es = es;
	}

	@Override
	public void created() throws InterruptedException {}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void render() {
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 4 + 1, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3i(255, 0, 0);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 4, "WORK IN PROGRESS. THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
	}

	@Override
	public void beforeRender(float dt) {

	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public boolean onKeyPressed(KeyPressedEvent k) {
		switch (k.getKey()) {
			case LETTER_X:
				//TODO: far funzionare questa parte
				/*HardwareDevice.INSTANCE.getDisplayManager().goBack();
				try {
					es.calc.solveExpression('X');
				} catch (final Error e) {
					final Screen scr = HardwareDevice.INSTANCE.getDisplayManager().getScreen();
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

}
