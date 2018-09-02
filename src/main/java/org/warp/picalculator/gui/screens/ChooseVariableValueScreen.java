package org.warp.picalculator.gui.screens;

import org.warp.picalculator.StaticVars;
import org.warp.picalculator.Utils;
import org.warp.picalculator.device.HardwareDevice;
import org.warp.picalculator.event.KeyPressedEvent;
import org.warp.picalculator.math.Function;
import org.warp.picalculator.math.functions.Variable.VariableValue;

public class ChooseVariableValueScreen extends Screen {

	@SuppressWarnings("unused")
	private final MathInputScreen es;
	public Function resultNumberValue;

	public ChooseVariableValueScreen(MathInputScreen es, VariableValue variableValue) {
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
		Utils.getFont(false, true).use(HardwareDevice.INSTANCE.getDisplayManager().engine);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 2 - 20, "WORK IN PROGRESS.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 2 - 20 + 1, "WORK IN PROGRESS.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 2 - 20 + 1, "WORK IN PROGRESS.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3i(255, 0, 0);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 2 - 20, "WORK IN PROGRESS.");

		Utils.getFont(false, false).use(HardwareDevice.INSTANCE.getDisplayManager().engine);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor4i(0, 0, 0, 64);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2 + 1, StaticVars.screenSize[1] / 2 + 1, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3i(255, 0, 0);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, StaticVars.screenSize[1] / 2, "THIS SCREEN MUST HAVE A GUI TO SELECT THE VARIABLE TO SOLVE.");
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

}
