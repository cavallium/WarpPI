package it.cavallium.warppi.gui;

import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.event.*;
import it.cavallium.warppi.gui.graphicengine.*;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.Timer;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class DisplayManager implements RenderingLoop {
	private static final int tickDuration = 50;

	private float brightness;

	public final DisplayOutputDevice display;
	public final GraphicEngine graphicEngine;
	public final BacklightOutputDevice backlight;
	public final boolean supportsPauses;
	public Renderer renderer;

	public Skin guiSkin;
	public BinaryFont[] fonts;

	public String error;
	public String[] errorStackTrace;
	public final int[] glyphsHeight;

	private Screen screen;
	private final HUD hud;
	private final String initialTitle;
	private Screen initialScreen;
	public Semaphore screenChange;
	public String displayDebugString;
	public ObjectArrayList<GUIErrorMessage> errorMessages;
	/**
	 * Set to true when an event is fired
	 */
	public boolean forceRefresh;

	public DisplayManager(final DisplayOutputDevice display, final BacklightOutputDevice backlight, final HUD hud, final Screen screen, final String title) {
		this.display = display;
		this.graphicEngine = display.getGraphicEngine();
		this.backlight = backlight;
		this.hud = hud;
		initialTitle = title;
		initialScreen = screen;

		screenChange = WarpPI.getPlatform().newSemaphore();
		supportsPauses = graphicEngine.doesRefreshPauses();

		glyphsHeight = new int[]{9, 6, 12, 9};
		displayDebugString = "";
		errorMessages = new ObjectArrayList<>();
	}

	public void initialize() {
		try {
			hud.d = this;
			hud.create();
			if (!hud.initialized) {
				hud.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			WarpPI.getPlatform().exit(0);
		}

		try {
			graphicEngine.create();
			renderer = graphicEngine.getRenderer();
			graphicEngine.setTitle(initialTitle);
			loop();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * private void load_skin() { try { skin_tex = glGenTextures();
	 * glBindTexture(GL_TEXTURE_2D, skin_tex); glPixelStorei(GL_UNPACK_ALIGNMENT,
	 * 1);
	 *
	 * InputStream in = new FileInputStream("skin.png"); PNGDecoder decoder = new
	 * PNGDecoder(in);
	 *
	 * System.out.println("width="+decoder.getWidth());
	 * System.out.println("height="+decoder.getHeight());
	 *
	 * ByteBuffer buf =
	 * ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
	 * decoder.decode(buf, decoder.getWidth()*4, Format.RGBA); buf.flip();
	 *
	 * skin = buf; skin_w = decoder.getWidth(); skin_h = decoder.getHeight();
	 * glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, skin_w, skin_h, 0, GL_RGBA,
	 * GL_UNSIGNED_BYTE, skin); } catch (IOException ex) { ex.printStackTrace(); } }
	 */

	public void closeScreen() {
		boolean isLastSession = sessions[1] == null;
		if (!isLastSession) {
			if (currentSession >= 0) {
				List<Screen> newSessions = new LinkedList<>();
				int i = 0;
				for (Screen s : sessions) {
					if (i != currentSession && s != null) {
						newSessions.add(s);
					}
					i++;
				}
				sessions = newSessions.toArray(new Screen[5]);
				if (currentSession >= newSessions.size()) {
					currentSession--;
				}
			} else {
				currentSession = 0;
			}
			updateCurrentScreen(sessions[currentSession]);
		}
	}

	public void setScreen(final Screen screen) {
		boolean mustBeAddedToHistory = screen.initialized == false;
		if (!mustBeAddedToHistory) {
			boolean found = false;
			for (Screen s : sessions) {
				found = found | s == screen;
			}
			mustBeAddedToHistory |= !found;
		}
		if (mustBeAddedToHistory) {
			if (screen.historyBehavior == HistoryBehavior.NORMAL
				|| screen.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
				if (currentSession > 0) {
					final int sl = sessions.length; // TODO: I don't know why if i don't add +5 or more some items
					// disappear
					List<Screen> newSessions = new LinkedList<>();
					int i = 0;
					for (Screen s : sessions) {
						if (i == currentSession) {
							currentSession = newSessions.size();
							newSessions.add(screen);
						}
						if (s != null) {
							if (i < currentSession) {
								if (s.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
									newSessions.add(s);
								}
							} else {
								if (s.historyBehavior != HistoryBehavior.DONT_KEEP_IN_HISTORY)
									newSessions.add(s);
							}
						}
						i++;
					}
					for (int j = 0; j < sl; j++) {
						if (j < newSessions.size()) {
							sessions[j] = newSessions.get(j);
						} else {
							sessions[j] = null;
						}
					}
				} else {
					currentSession = 0;
					for (int i = sessions.length - 1; i >= 1; i--) {
						sessions[i] = sessions[i - 1];
					}
					sessions[0] = screen;
				}
			} else {
				currentSession = -1;
			}
		}
		updateCurrentScreen(screen);
	}

	private void updateCurrentScreen(Screen screen) {
		screen.d = this;
		try {
			if (screen.created == false) {
				screen.create();
			}
			this.screen = screen;
			if (screen.initialized == false) {
				screen.initialize();
			}
			screenChange.release();
		} catch (final Exception e) {
			e.printStackTrace();
			WarpPI.getPlatform().exit(0);
		}
	}

	public void replaceScreen(final Screen screen) {
		if (screen.initialized == false) {
			if (screen.historyBehavior == HistoryBehavior.NORMAL
				|| screen.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
				sessions[currentSession] = screen;
			} else {
				currentSession = -1;
				for (int i = 0; i < sessions.length - 2; i++) {
					sessions[i] = sessions[i + 1];
				}
			}
		}
		screen.d = this;
		try {
			screen.create();
			this.screen = screen;
			if (screen.initialized == false) {
				screen.initialize();
			}
			screenChange.release();
		} catch (final Exception e) {
			e.printStackTrace();
			WarpPI.getPlatform().exit(0);
		}
	}

	public boolean canGoBack() {
		if (currentSession <= -1) {
			return sessions[0] != null;
		}
		if (screen != sessions[currentSession]) {

		} else if (currentSession + 1 < sessions.length) {
			if (sessions[currentSession + 1] != null) {

			} else {
				return false;
			}
		} else {
			return false;
		}
		if (sessions[currentSession] != null) {
			return true;
		}
		return false;
	}

	public void goBack() {
		if (canGoBack()) {
			if (currentSession >= 0 && screen != sessions[currentSession]) {
			} else {
				currentSession += 1;
			}
			screen = sessions[currentSession];
			screenChange.release();
		}
	}

	public boolean canGoForward() {
		if (currentSession <= 0) {
			return false;
		}
		if (screen != sessions[currentSession]) {

		} else if (currentSession > 0) {
			if (sessions[currentSession - 1] != null) {

			} else {
				return false;
			}
		} else {
			return false;
		}
		if (sessions[currentSession] != null) {
			return true;
		}
		return false;
	}

	public void goForward() {
		if (canGoForward()) {
			if (screen != sessions[currentSession]) {

			} else {
				currentSession -= 1;
			}
			screen = sessions[currentSession];
			screenChange.release();
		}
	}

	public Screen getScreen() {
		return screen;
	}

	public HUD getHUD() {
		return hud;
	}

	private void load_skin() throws IOException {
		guiSkin = graphicEngine.loadSkin("/skin.png");
	}

	private void load_fonts() throws IOException {
		fonts = new BinaryFont[7];
		fonts[0] = graphicEngine.loadFont("smal");
		fonts[1] = graphicEngine.loadFont("smallest");
		fonts[2] = graphicEngine.loadFont("norm");
		fonts[3] = graphicEngine.loadFont("smal");
		// 4
		// fonts[5] = engine.loadFont("square");
	}

	private void draw_init() {
		if (graphicEngine.supportsFontRegistering()) {
			final List<BinaryFont> fontsIterator = graphicEngine.getRegisteredFonts();
			for (final BinaryFont f : fontsIterator) {
				if (!f.isInitialized()) {
					f.initialize(display);
				}
			}
		}
		if (!screen.graphicInitialized) {
			try {
				var displaySize = display.getDisplaySize();
				var scrWidth = displaySize[0] - hud.getMarginLeft() - hud.getMarginRight();
				var scrHeight = displaySize[1] - hud.getMarginTop() - hud.getMarginBottom();
				var scrCtx = new ScreenContext(graphicEngine, scrWidth, scrHeight);
				screen.initializeGraphic(scrCtx);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		renderer.glClear(graphicEngine.getWidth(), graphicEngine.getHeight());
	}

	private void draw_world() {
		var displaySize = display.getDisplaySize();
		var scrWidth = displaySize[0] - hud.getMarginLeft() - hud.getMarginRight();
		var scrHeight = displaySize[1] - hud.getMarginTop() - hud.getMarginBottom();
		var scrCtx = new RenderContext(graphicEngine, renderer.getBoundedInstance(hud.getMarginLeft(), hud.getMarginTop(), scrWidth, scrHeight), scrWidth, scrHeight);
		var fullCtx = new RenderContext(graphicEngine, renderer, displaySize[0], displaySize[1]);

		renderer.glColor3i(255, 255, 255);

		if (error != null) {
			final BinaryFont fnt = Utils.getFont(false, false);
			if (fnt != null && fnt != graphicEngine.getRenderer().getCurrentFont()) {
				fnt.use(display);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringRight(display.getDisplaySize()[0] - 2,
				display.getDisplaySize()[1] - (fnt.getCharacterHeight() + 2),
				WarpPI.getPlatform().getSettings().getCalculatorNameUppercase() + " CALCULATOR");
			renderer.glColor3i(149, 32, 26);
			renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, 22, error);
			renderer.glColor3i(164, 34, 28);
			int i = 22;
			for (final String stackPart : errorStackTrace) {
				renderer.glDrawStringLeft(2, 22 + i, stackPart);
				i += 11;
			}
			if (fonts[0] != null && fonts[0] != graphicEngine.getRenderer().getCurrentFont()) {
				fonts[0].use(display);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringCenter(display.getDisplaySize()[0] / 2, 11, "UNEXPECTED EXCEPTION");
		} else {
			if (fonts[0] != null && fonts[0] != graphicEngine.getRenderer().getCurrentFont()) {
				fonts[0].use(display);
			}
			if (hud.visible)
				hud.renderBackground();
			screen.render(scrCtx);
			if (hud.visible) {
				hud.render(fullCtx);
				hud.renderTopmostBackground();
			}
			screen.renderTopmost(fullCtx);
			if (hud.visible)
				hud.renderTopmost(fullCtx);
		}
	}

	private void draw() {
		draw_init();
		draw_world();
	}

	private long precTime = -1;

	@Override
	public void refresh(boolean force) {
		if (force || supportsPauses == false || Keyboard.popRefreshRequest() || forceRefresh || screen.mustBeRefreshed()) {
			forceRefresh = false;
			draw();
		}

	}

	public void loop() {
		try {
			load_skin();
			load_fonts();

			try {
				if (initialScreen != null) {
					setScreen(initialScreen);
					initialScreen = null;
				}
			} catch (final Exception e) {
				e.printStackTrace();
				WarpPI.getPlatform().exit(0);
			}

			var displayRefreshManager = new DisplayRefreshManager(this::onRefresh);
			new Timer(DisplayManager.tickDuration, displayRefreshManager::onTick);
			graphicEngine.onResize().subscribe(displayRefreshManager::onResize);

			graphicEngine.start(getDrawable());
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
		}
	}

	private void onRefresh(Integer[] windowSize) {
		double dt = 0;
		final long newtime = System.nanoTime();
		if (precTime == -1) {
			dt = DisplayManager.tickDuration;
		} else {
			dt = (newtime - precTime) / 1000d / 1000d;
		}
		precTime = newtime;

		if (windowSize != null) {
			display.getDisplaySize()[0] = windowSize[0];
			display.getDisplaySize()[1] = windowSize[1];
		}

		var displaySize = display.getDisplaySize();
		var scrWidth = displaySize[0] - hud.getMarginLeft() - hud.getMarginRight();
		var scrHeight = displaySize[1] - hud.getMarginTop() - hud.getMarginBottom();
		var scrCtx = new ScreenContext(graphicEngine, scrWidth, scrHeight);
		screen.beforeRender(scrCtx, (float) (dt / 1000d));
	}

	public void changeBrightness(final float change) {
		setBrightness(brightness + change);
	}

	public void setBrightness(final float newval) {
		if (newval >= 0 && newval <= 1) {
			brightness = newval;
			backlight.setBrightness(brightness);
		}
	}

	public void cycleBrightness(final boolean reverse) {
		final float step = reverse ? -0.1f : 0.1f;
		if (brightness + step > 1f) {
			setBrightness(0f);
		} else if (brightness + step <= 0f) {
			setBrightness(1.0f);
		} else {
			changeBrightness(step);
		}
	}

	public float getBrightness() {
		return brightness;
	}

	public int currentSession = 0;
	public Screen[] sessions = new Screen[5];

	@Deprecated
	public void colore(final float f1, final float f2, final float f3, final float f4) {
		renderer.glColor4f(f1, f2, f3, f4);
	}

	public RenderingLoop getDrawable() {
		return this;
	}

	@Deprecated
	public void drawSkinPart(final int x, final int y, final int uvX, final int uvY, final int uvX2, final int uvY2) {
		renderer.glFillRect(x, y, uvX2 - uvX, uvY2 - uvY, uvX, uvY, uvX2 - uvX, uvY2 - uvY);
	}

	public Consumer<TouchEvent> getTouchEventListener() {
		return (TouchEvent t) -> {
			boolean refresh = false;
			if (screen != null && screen.initialized && executeTouchEventOnScreen(t, screen)) {
				refresh = true;
			} else {
				//Default behavior
			}
			if (refresh) {
				forceRefresh = true;
			}
		};
	}

	private boolean executeTouchEventOnScreen(TouchEvent t, Screen scr) {
		if (t instanceof TouchStartEvent) {
			return scr.onTouchStart((TouchStartEvent) t);
		} else if (t instanceof TouchMoveEvent) {
			return scr.onTouchMove((TouchMoveEvent) t);
		} else if (t instanceof TouchEndEvent) {
			return scr.onTouchEnd((TouchEndEvent) t);
		} else if (t instanceof TouchCancelEvent) {
			return scr.onTouchCancel((TouchCancelEvent) t);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}