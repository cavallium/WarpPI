package it.cavallium.warppi.gui;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
		final Renderer r = d.renderer;
		final GraphicEngine engine = d.engine;

		r.glColor(0xFFc5c2af);
		r.glFillColor(0, 0, engine.getWidth(), 20);
	}

	@Override
	public void renderTopmost() {
		final Renderer r = d.renderer;
		final GraphicEngine engine = d.engine;
		final Skin guiSkin = d.guiSkin;

		//DRAW TOP
		r.glColor3i(0, 0, 0);
		r.glDrawLine(0, 20, engine.getWidth() - 1, 20);
		r.glColor3i(255, 255, 255);
		guiSkin.use(engine);
		if (Keyboard.shift) {
			r.glFillRect(2 + 18 * 0, 2, 16, 16, 16 * 2, 16 * 0, 16, 16);
		} else {
			r.glFillRect(2 + 18 * 0, 2, 16, 16, 16 * 3, 16 * 0, 16, 16);
		}
		if (Keyboard.alpha) {
			r.glFillRect(2 + 18 * 1, 2, 16, 16, 16 * 0, 16 * 0, 16, 16);
		} else {
			r.glFillRect(2 + 18 * 1, 2, 16, 16, 16 * 1, 16 * 0, 16, 16);
		}

		int padding = 2;

		final int brightness = (int) Math.ceil(Engine.INSTANCE.getHardwareDevice().getDisplayManager().getBrightness() * 9);
		if (brightness <= 10) {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * brightness, 16 * 1, 16, 16);
		} else {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Brightness error");
		}

		padding += 18 + 6;

		final boolean canGoBack = Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoBack();
		final boolean canGoForward = Engine.INSTANCE.getHardwareDevice().getDisplayManager().canGoForward();

		if (Engine.getPlatform().getSettings().isDebugEnabled()) {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 18, 16 * 0, 16, 16);
			padding += 18 + 6;
		}

		if (canGoBack && canGoForward) {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 14, 16 * 0, 16, 16);
		} else if (canGoBack) {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 15, 16 * 0, 16, 16);
		} else if (canGoForward) {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 16, 16 * 0, 16, 16);
		} else {
			r.glFillRect(StaticVars.screenSize[0] - (padding + 16), 2, 16, 16, 16 * 17, 16 * 0, 16, 16);
		}

		padding += 18;

		//DRAW BOTTOM
		r.glDrawStringLeft(2, 90, d.displayDebugString);

		Utils.getFont(true, false).use(engine);
		r.glColor4i(255, 0, 0, 40);
		r.glDrawStringLeft(1 + 1, StaticVars.screenSize[1] - 7 - 7 + 1, "WORK IN");
		r.glColor4i(255, 0, 0, 80);
		r.glDrawStringLeft(1, StaticVars.screenSize[1] - 7 - 7, "WORK IN");
		r.glColor4i(255, 0, 0, 40);
		r.glDrawStringLeft(1 + 1, StaticVars.screenSize[1] - 7 + 1, "PROGRESS.");
		r.glColor4i(255, 0, 0, 80);
		r.glDrawStringLeft(1, StaticVars.screenSize[1] - 7, "PROGRESS.");
		
		int currentDebugLine = 2;
		if (Engine.getPlatform().getSettings().isDebugEnabled()) {
			ObjectArrayList<Screen> allSessions = new ObjectArrayList<>();
			for (Screen session : Engine.INSTANCE.getHardwareDevice().getDisplayManager().sessions) {
				allSessions.add(0, session);
			}
			Screen curScreen = Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			if (curScreen.historyBehavior == HistoryBehavior.DONT_KEEP_IN_HISTORY) {
				allSessions.add(curScreen);
			}
			
			for (Screen session : allSessions) {
				if (session != null) {
					String title = session.getSessionTitle();
					if (title != null && title.length() > 0) {
						Utils.getFont(true).use(engine);
						if (session.historyBehavior == HistoryBehavior.DONT_KEEP_IN_HISTORY) {
							r.glColor(0xFF3333FF);
						} else if (session.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
							r.glColor(0xFFFF33FF);
						} else {
							r.glColor(0xFF990000);
						}
						r.glDrawStringLeft(0, StaticVars.screenSize[1] - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "[" + String.format("%1$03d", session.debugScreenID) + "] " + title.toUpperCase());
						if (session == Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen()) {
							r.glColor(0xFF00CC00);
						} else {
							r.glColor(0xFF990000);
						}
						r.glDrawStringLeft(0, StaticVars.screenSize[1] - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "      " + title.toUpperCase());
					}	
					currentDebugLine++;
				}
			}
			r.glColor(0xFF000000);
			r.glDrawStringLeft(5, StaticVars.screenSize[1] - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "DEBUG ENABLED");
		}
	}

	@Override
	public void beforeRender(final float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderBackground() {
		// TODO Auto-generated method stub

	}

}
