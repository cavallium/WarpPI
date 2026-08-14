package it.cavallium.warppi.gui;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CalculatorHUD extends HUD {
	private static final int BACKGROUND_COLOR = 0xFFC5C2AF;
	static final int STATUS_INDICATOR_X = 40;
	static final int STATUS_INDICATOR_Y = 2;

	@Override
	public void created() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialized() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void graphicInitialized() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(RenderContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderTopmostBackground() {
		final Renderer r = d.renderer;
		final DisplayOutputDevice display = d.display;
		final GraphicEngine engine = display.getGraphicEngine();

		r.glColor(BACKGROUND_COLOR);
		r.glFillColor(0, 0, engine.getWidth(), 20);
	}

	@Override
	public int getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	@Override
	public void renderTopmost(RenderContext ctx) {
		final Renderer r = ctx.getRenderer();
		final DisplayOutputDevice display = d.display;
		final GraphicEngine engine = display.getGraphicEngine();
		final Skin guiSkin = d.guiSkin;

		//DRAW TOP
		r.glColor3i(0, 0, 0);
		r.glDrawLine(0, 20, engine.getWidth() - 1, 20);
		r.glColor3i(255, 255, 255);
		guiSkin.use(display);
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
		if (WarpPI.INSTANCE.isLoading()) {
			renderLoadingIndicator(r, 41, 4);
		}

		int padding = 2;
		if (d.supportsBrightnessControl()) {
			final int brightness = Math.max(0, Math.min(9,
					(int) Math.ceil(d.getBrightness() * 9)));
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16,
					16 * brightness, 16, 16, 16);
			padding += 18 + 6;
		}

		final boolean canGoBack = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().canGoBack();
		final boolean canGoForward = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().canGoForward();

		if (WarpPI.getPlatform().getSettings().isDebugEnabled()) {
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16, 16 * 18, 16 * 0, 16, 16);
			padding += 18 + 6;
		}

		if (canGoBack && canGoForward) {
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16, 16 * 14, 16 * 0, 16, 16);
		} else if (canGoBack) {
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16, 16 * 15, 16 * 0, 16, 16);
		} else if (canGoForward) {
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16, 16 * 16, 16 * 0, 16, 16);
		} else {
			r.glFillRect(ctx.getWidth() - (padding + 16), 2, 16, 16, 16 * 17, 16 * 0, 16, 16);
		}

		padding += 18;

		//DRAW BOTTOM
		r.glDrawStringLeft(2, 90, d.displayDebugString);

		Utils.getFont(true, false).use(display);
		r.glColor4i(255, 0, 0, 40);
		r.glDrawStringLeft(1 + 1, ctx.getHeight() - 7 - 7 + 1, "WORK IN");
		r.glColor4i(255, 0, 0, 80);
		r.glDrawStringLeft(1, ctx.getHeight() - 7 - 7, "WORK IN");
		r.glColor4i(255, 0, 0, 40);
		r.glDrawStringLeft(1 + 1, ctx.getHeight() - 7 + 1, "PROGRESS.");
		r.glColor4i(255, 0, 0, 80);
		r.glDrawStringLeft(1, ctx.getHeight() - 7, "PROGRESS.");
		
		int currentDebugLine = 2;
		if (WarpPI.getPlatform().getSettings().isDebugEnabled()) {
			ObjectArrayList<Screen> allSessions = new ObjectArrayList<>();
			for (Screen session : WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().sessions) {
				allSessions.add(0, session);
			}
			Screen curScreen = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen();
			if (curScreen.historyBehavior == HistoryBehavior.DONT_KEEP_IN_HISTORY) {
				allSessions.add(curScreen);
			}
			
			for (Screen session : allSessions) {
				if (session != null) {
					String title = session.getSessionTitle();
					if (title != null && title.length() > 0) {
						Utils.getFont(true).use(display);
						if (session.historyBehavior == HistoryBehavior.DONT_KEEP_IN_HISTORY) {
							r.glColor(0xFF3333FF);
						} else if (session.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
							r.glColor(0xFFFF33FF);
						} else {
							r.glColor(0xFF990000);
						}
						r.glDrawStringLeft(0, ctx.getHeight() - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "[" + String.format("%1$03d", session.debugScreenID) + "] " + title.toUpperCase());
						if (session == WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen()) {
							r.glColor(0xFF00CC00);
						} else {
							r.glColor(0xFF990000);
						}
						r.glDrawStringLeft(0, ctx.getHeight() - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "      " + title.toUpperCase());
					}	
					currentDebugLine++;
				}
			}
			r.glColor(0xFF000000);
			r.glDrawStringLeft(5, ctx.getHeight() - ((currentDebugLine+1) * (r.getCurrentFont().getCharacterHeight()+1)), "DEBUG ENABLED");
		}
	}

	static void renderLoadingIndicator(final Renderer renderer, final int x, final int y) {
		renderer.glColor3i(0, 0, 0);
		renderer.glFillColor(x, y, 10, 2);
		renderer.glFillColor(x + 1, y + 2, 8, 2);
		renderer.glFillColor(x + 4, y + 4, 2, 4);
		renderer.glFillColor(x + 1, y + 8, 8, 2);
		renderer.glFillColor(x, y + 10, 10, 2);
		renderer.glColor3i(255, 255, 255);
	}

	/**
	 * Draws an opaque, texture-free crash marker. Keeping this independent of the
	 * skin lets the display manager draw it even when texture or HUD rendering is
	 * the code path that failed.
	 */
	static void renderCrashedIndicator(final Renderer renderer, final int x, final int y) {
		renderer.glColor3i(179, 38, 30);
		renderer.glFillColor(x, y, 16, 16);

		renderer.glColor3i(255, 255, 255);
		renderer.glFillColor(x + 3, y + 3, 2, 2);
		renderer.glFillColor(x + 11, y + 3, 2, 2);
		renderer.glFillColor(x + 5, y + 5, 2, 2);
		renderer.glFillColor(x + 9, y + 5, 2, 2);
		renderer.glFillColor(x + 7, y + 7, 2, 2);
		renderer.glFillColor(x + 5, y + 9, 2, 2);
		renderer.glFillColor(x + 9, y + 9, 2, 2);
		renderer.glFillColor(x + 3, y + 11, 2, 2);
		renderer.glFillColor(x + 11, y + 11, 2, 2);
	}

	@Override
	public void beforeRender(ScreenContext ctx, final float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMarginLeft() {
		return 0;
	}

	@Override
	public int getMarginTop() {
		return 20;
	}

	@Override
	public int getMarginRight() {
		return 0;
	}

	@Override
	public int getMarginBottom() {
		return 0;
	}

	@Override
	public void renderBackground() {
		// TODO Auto-generated method stub

	}

}
