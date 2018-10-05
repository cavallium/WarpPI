package it.cavallium.warppi.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.graphicengine.impl.nogui.NoGuiEngine;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class DisplayManager implements RenderingLoop {
	private static final int tickDuration = 50;

	private float brightness;

	public final GraphicEngine engine;
	public final HardwareDisplay monitor;
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

	public DisplayManager(final HardwareDisplay monitor, final HUD hud, final Screen screen, final String title) {
		this.monitor = monitor;
		this.hud = hud;
		initialTitle = title;
		initialScreen = screen;

		screenChange = Engine.getPlatform().newSemaphore();
		engine = chooseGraphicEngine();
		supportsPauses = engine.doesRefreshPauses();

		glyphsHeight = new int[] { 9, 6, 12, 9 };
		displayDebugString = "";
		errorMessages = new ObjectArrayList<>();
	}

	public void initialize() {
		monitor.initialize();

		try {
			hud.d = this;
			hud.create();
			if (!hud.initialized) {
				hud.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
		}

		try {
			engine.create();
			renderer = engine.getRenderer();
			engine.setTitle(initialTitle);
			loop();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		monitor.shutdown();
	}

	/*
	 * private void load_skin() {
	 * try {
	 * skin_tex = glGenTextures();
	 * glBindTexture(GL_TEXTURE_2D, skin_tex);
	 * glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	 *
	 * InputStream in = new FileInputStream("skin.png");
	 * PNGDecoder decoder = new PNGDecoder(in);
	 *
	 * System.out.println("width="+decoder.getWidth());
	 * System.out.println("height="+decoder.getHeight());
	 *
	 * ByteBuffer buf =
	 * ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
	 * decoder.decode(buf, decoder.getWidth()*4, Format.RGBA);
	 * buf.flip();
	 *
	 * skin = buf;
	 * skin_w = decoder.getWidth();
	 * skin_h = decoder.getHeight();
	 * glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, skin_w,
	 * skin_h, 0, GL_RGBA, GL_UNSIGNED_BYTE, skin);
	 * } catch (IOException ex) {
	 * ex.printStackTrace();
	 * }
	 * }
	 */

	private GraphicEngine chooseGraphicEngine() {
		GraphicEngine d;
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "framebuffer engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using FB Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "CPU engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using CPU Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "GPU engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using GPU Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 24 bit engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless 24 bit Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 256 colors engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless 256 Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 8 colors engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless basic Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "HTML5 engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "Using Html Graphic Engine");
			return d;
		}
		d = new NoGuiEngine();
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using NoGui Graphic Engine");
			return d;
		}
		throw new UnsupportedOperationException("No graphic engines available.");
	}



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
			if (screen.historyBehavior == HistoryBehavior.NORMAL || screen.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
				if (currentSession > 0) {
					final int sl = sessions.length; //TODO: I don't know why if i don't add +5 or more some items disappear
					List<Screen> newSessions = new LinkedList<>();
					int i = 0;
					for (Screen s : sessions) {
						if (s != null) {
							if (i < currentSession) {
								if (s.historyBehavior != HistoryBehavior.DONT_KEEP_IN_HISTORY)
									newSessions.add(s);
							} else {
								if (s.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
									newSessions.add(s);
								}
							}
						}
						i++;
					}
					sessions = newSessions.toArray(new Screen[5]);
					currentSession = newSessions.indexOf(screen);
//					sessions = Arrays.copyOfRange(sessions, currentSession, sl);
				} else {
					currentSession = 0;
				}
				for (int i = sessions.length - 1; i >= 1; i--) {
					sessions[i] = sessions[i - 1];
				}
				sessions[0] = screen;
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
			screenChange.release();
			if (screen.initialized == false) {
				screen.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
		}
	}

	public void replaceScreen(final Screen screen) {
		if (screen.initialized == false) {
			if (screen.historyBehavior == HistoryBehavior.NORMAL || screen.historyBehavior == HistoryBehavior.ALWAYS_KEEP_IN_HISTORY) {
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
			screenChange.release();
			if (screen.initialized == false) {
				screen.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
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
			if (currentSession >= 0 && screen != sessions[currentSession]) {} else {
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
		guiSkin = engine.loadSkin("/skin.png");
	}

	private void load_fonts() throws IOException {
		fonts = new BinaryFont[7];
		fonts[0] = engine.loadFont("smal");
		fonts[1] = engine.loadFont("smallest");
		fonts[2] = engine.loadFont("norm");
		fonts[3] = engine.loadFont("smal");
		//4
		//fonts[5] = engine.loadFont("square");
	}

	private void draw_init() {
		if (engine.supportsFontRegistering()) {
			final List<BinaryFont> fontsIterator = engine.getRegisteredFonts();
			for (final BinaryFont f : fontsIterator) {
				if (!f.isInitialized()) {
					f.initialize(engine);
				}
			}
		}
		renderer.glClear(engine.getWidth(), engine.getHeight());
	}

	private void draw_world() {
		renderer.glColor3i(255, 255, 255);

		if (error != null) {
			final BinaryFont fnt = Utils.getFont(false, false);
			if (fnt != null && fnt != engine.getRenderer().getCurrentFont()) {
				fnt.use(engine);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringRight(StaticVars.screenSize[0] - 2, StaticVars.screenSize[1] - (fnt.getCharacterHeight() + 2), Engine.getPlatform().getSettings().getCalculatorNameUppercase() + " CALCULATOR");
			renderer.glColor3i(149, 32, 26);
			renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, 22, error);
			renderer.glColor3i(164, 34, 28);
			int i = 22;
			for (final String stackPart : errorStackTrace) {
				renderer.glDrawStringLeft(2, 22 + i, stackPart);
				i += 11;
			}
			if (fonts[0] != null && fonts[0] != engine.getRenderer().getCurrentFont()) {
				fonts[0].use(engine);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringCenter(StaticVars.screenSize[0] / 2, 11, "UNEXPECTED EXCEPTION");
		} else {
			if (fonts[0] != null && fonts[0] != engine.getRenderer().getCurrentFont()) {
				fonts[0].use(engine);
			}
			if (hud.visible) hud.renderBackground();
			screen.render();
			if (hud.visible) {
				hud.render();
				hud.renderTopmostBackground();
			}
			screen.renderTopmost();
			if (hud.visible) hud.renderTopmost();
		}
	}

	private void draw() {
		draw_init();
		draw_world();
	}

	private long precTime = -1;

	@Override
	public void refresh() {
		if (supportsPauses == false || Keyboard.popRefreshRequest() || forceRefresh || screen.mustBeRefreshed()) {
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
				screen.initialize();
			} catch (final Exception e) {
				e.printStackTrace();
				Engine.getPlatform().exit(0);
			}

			final Observable<Long> workTimer = Observable.interval(DisplayManager.tickDuration);

			final Observable<Integer[]> onResizeObservable = engine.onResize();
			Observable<Pair<Long, Integer[]>> refreshObservable;
			if (onResizeObservable == null) {
				refreshObservable = workTimer.map((l) -> Pair.of(l, null));
			} else {
				refreshObservable = Observable.combineChanged(workTimer, engine.onResize());
			}

			refreshObservable.subscribe((pair) -> {
				double dt = 0;
				final long newtime = System.nanoTime();
				if (precTime == -1) {
					dt = DisplayManager.tickDuration;
				} else {
					dt = (newtime - precTime) / 1000d / 1000d;
				}
				precTime = newtime;

				if (pair.getRight() != null) {
					final Integer[] windowSize = pair.getRight();
					StaticVars.screenSize[0] = windowSize[0];
					StaticVars.screenSize[1] = windowSize[1];
				}

				screen.beforeRender((float) (dt / 1000d));
			});

			engine.start(getDrawable());
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {}
	}

	public void changeBrightness(final float change) {
		setBrightness(brightness + change);
	}

	public void setBrightness(final float newval) {
		if (newval >= 0 && newval <= 1) {
			brightness = newval;
			monitor.setBrightness(brightness);
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

	public void waitForExit() {
		engine.waitForExit();
	}
}