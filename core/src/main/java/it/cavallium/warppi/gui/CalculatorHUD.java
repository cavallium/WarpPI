package it.cavallium.warppi.gui;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.util.Utils;

public class CalculatorHUD extends HUD {

	@Override
	public void created() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialized() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderTopmostBackground() {
		final Renderer renderer = d.renderer;
		final GraphicEngine engine = d.engine;

		renderer.glColor(0xFFc5c2af);
		renderer.glFillColor(0, 0, engine.getWidth(), 20);
	}

	@Override
	public void renderTopmost() {
		final Renderer renderer = d.renderer;
		final GraphicEngine engine = d.engine;
		final Skin guiSkin = d.guiSkin;

		//DRAW TOP
		renderer.glColor3i(0, 0, 0);
		renderer.glDrawLine(0, 20, engine.getWidth() - 1, 20);
		renderer.glColor3i(255, 255, 255);
		guiSkin.use(engine);
		if (Keyboard.shift) {
			renderer.glFillRect(2 + 18 * 0, 2, 16, 16, 16 * 2, 16 * 0, 16, 16);
		} else {
			renderer.glFillRect(2 + 18 * 0, 2, 16, 16, 16 * 3, 16 * 0, 16, 16);
		}
		if (Keyboard.alpha) {
			renderer.glFillRect(2 + 18 * 1, 2, 16, 16, 16 * 0, 16 * 0, 16, 16);
		} else {
			renderer.glFillRect(2 + 18 * 1, 2, 16, 16, 16 * 1, 16 * 0, 16, 16);
		}
		/*
		if (Calculator.angleMode == AngleMode.DEG) {
			drawSkinPart(8 + 18 * 2, 2, 16 * 4, 16 * 0, 16 + 16 * 4, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 3, 2, 16 * 7, 16 * 0, 16 + 16 * 7, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 4, 2, 16 * 9, 16 * 0, 16 + 16 * 9, 16 + 16 * 0);
		} else if (Calculator.angleMode == AngleMode.RAD) {
			drawSkinPart(8 + 18 * 2, 2, 16 * 5, 16 * 0, 16 + 16 * 5, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 3, 2, 16 * 6, 16 * 0, 16 + 16 * 6, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 4, 2, 16 * 9, 16 * 0, 16 + 16 * 9, 16 + 16 * 0);
		} else if (Calculator.angleMode == AngleMode.GRA) {
			drawSkinPart(8 + 18 * 2, 2, 16 * 5, 16 * 0, 16 + 16 * 5, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 3, 2, 16 * 7, 16 * 0, 16 + 16 * 7, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 4, 2, 16 * 8, 16 * 0, 16 + 16 * 8, 16 + 16 * 0);
		} else {
			drawSkinPart(8 + 18 * 2, 2, 16 * 5, 16 * 0, 16 + 16 * 5, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 3, 2, 16 * 7, 16 * 0, 16 + 16 * 7, 16 + 16 * 0);
			drawSkinPart(8 + 18 * 4, 2, 16 * 9, 16 * 0, 16 + 16 * 9, 16 + 16 * 0);
		}*/

		int padding = 2;

		final int brightness = (int) (Math.ceil(Engine.INSTANCE.getHardwareDevice().getDisplayManager().getBrightness() * 9));
		if (brightness <= 10) {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * brightness, 16 * 1, 16, 16);
		} else {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Brightness error");
		}

		padding += 18 + 6;

		final boolean canGoBack = Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoBack();
		final boolean canGoForward = Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoForward();

		if (Engine.getPlatform().getSettings().isDebugEnabled()) {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 18, 16 * 0, 16, 16);
			padding += 18 + 6;
		}

		if (canGoBack && canGoForward) {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 14, 16 * 0, 16, 16);
		} else if (canGoBack) {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 15, 16 * 0, 16, 16);
		} else if (canGoForward) {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 16, 16 * 0, 16, 16);
		} else {
			renderer.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 17, 16 * 0, 16, 16);
		}

		padding += 18;

		//DRAW BOTTOM
		d.renderer.glDrawStringLeft(2, 90, d.displayDebugString);

		Utils.getFont(true, false).use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(255, 0, 0, 40);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringLeft(1 + 1, StaticVars.screenSize[1] - 7 - 7 + 1, "WORK IN");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(255, 0, 0, 80);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringLeft(1, StaticVars.screenSize[1] - 7 - 7, "WORK IN");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(255, 0, 0, 40);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringLeft(1 + 1, StaticVars.screenSize[1] - 7 + 1, "PROGRESS.");
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor4i(255, 0, 0, 80);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glDrawStringLeft(1, StaticVars.screenSize[1] - 7, "PROGRESS.");
	}

	@Override
	public void beforeRender(float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderBackground() {
		// TODO Auto-generated method stub

	}

}
