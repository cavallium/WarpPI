package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.util.EventSubscriber;

public class JOGLEngine implements GraphicEngine {
	private static final String RASPBERRY_PRODUCTION_PROPERTY = "warppi.production.raspberry";

	private volatile boolean initialized;
	private volatile boolean created;
	private boolean raspberryProductionMode;
	private NEWTWindow wnd;
	private RenderingLoop d;
	private JOGLRenderer r;
	private final Map<String, JOGLFont> fontCache = new HashMap<>();
	private volatile JOGLFont keyboardFont;
	private volatile Layer keyboardLayer = Layer.NORMAL;
	int[] size;
	private final CopyOnWriteArrayList<BinaryFont> registeredFonts = new CopyOnWriteArrayList<>();
	private final Semaphore exitSemaphore = new Semaphore(0);
	protected LinkedList<Texture> registeredTextures;
	protected LinkedList<Texture> unregisteredTextures;

	public JOGLEngine(int defaultWidth, int defaultHeight) {
		this.size = new int[]{defaultWidth, defaultHeight};
	}

	@Override
	public int[] getSize() {
		return size.clone();
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(final String title) {
		wnd.window.setTitle(title);
	}

	@Override
	public void setResizable(final boolean r) {
		if (!r)
			wnd.window.setPosition(0, 0);
		wnd.window.setResizable(r);
		wnd.window.setUndecorated(!r);
		wnd.window.setPointerVisible(!raspberryProductionMode);
	}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		wnd.setSize(ww, wh);
	}

	@Override
	public void create() {
		create(null);
	}

	@Override
	public void create(final Runnable onInitialized) {
		initialized = false;
		created = false;
		raspberryProductionMode = Boolean.getBoolean(RASPBERRY_PRODUCTION_PROPERTY)
				|| WarpPI.getPlatform().isRunningOnRaspberry();
		keyboardLayer = HardwareKeyboardLayout.activeLayer();
		this.getSize();
		size = new int[] { this.getSize()[0], this.getSize()[1] };
		created = true;
		registeredTextures = new LinkedList<>();
		unregisteredTextures = new LinkedList<>();
		r = new JOGLRenderer();
		wnd = new NEWTWindow(this);
		wnd.create();
		setDisplayMode(this.getSize()[0], this.getSize()[1]);
		setResizable(!raspberryProductionMode);
		if (raspberryProductionMode && !wnd.window.setFullscreen(true))
			throw new IllegalStateException("NEWT rejected Raspberry production fullscreen mode");
		initialized = true;
		wnd.onInitialized = onInitialized;
	}

	boolean isRaspberryProductionMode() {
		return raspberryProductionMode;
	}

	Layer getKeyboardLayer() {
		return keyboardLayer;
	}

	JOGLFont getKeyboardFont() {
		return keyboardFont;
	}

	/**
	 * INTERNAL USE ONLY!
	 * @return
	 */
	public GLWindow getGLWindow() {
		return wnd.window;
	}
	
	@Override
	public EventSubscriber<Integer[]> onResize() {
		return wnd.onResizeEvent;
	}

	@Override
	public int getWidth() {
		return size[0];
	}

	@Override
	public int getHeight() {
		return size[1];
	}

	@Override
	public void destroy() {
		if (initialized && created) {
			initialized = false;
			created = false;
			exitSemaphore.release();
			wnd.window.destroy();
		}
	}

	@Override
	public void start(final RenderingLoop d) {
		this.d = d;
		wnd.window.setVisible(true);
		wnd.window.setPointerVisible(!raspberryProductionMode);
		if (raspberryProductionMode) {
			final int screenWidth = wnd.window.getScreen().getWidth();
			final int screenHeight = wnd.window.getScreen().getHeight();
			if (!wnd.window.isFullscreen()
					|| wnd.window.getX() != 0
					|| wnd.window.getY() != 0
					|| wnd.window.getSurfaceWidth() != screenWidth
					|| wnd.window.getSurfaceHeight() != screenHeight) {
				throw new IllegalStateException("NEWT did not create an exact fullscreen surface: window="
						+ wnd.window.getX() + "," + wnd.window.getY() + " "
						+ wnd.window.getSurfaceWidth() + "x" + wnd.window.getSurfaceHeight()
						+ ", screen=" + screenWidth + "x" + screenHeight);
			}
			System.out.println("NEWT Raspberry production fullscreen: " + screenWidth + "x" + screenHeight);
		}
	}

	public void waitForExit() throws InterruptedException {
		exitSemaphore.acquire();
	}

	@Override
	public void repaint() {
		if (d != null & r != null && JOGLRenderer.gl != null)
			d.refresh(false);
	}

	@Override
	public void keyboardLayerChanged(final Layer layer) {
		keyboardLayer = Objects.requireNonNull(layer, "layer");
		final NEWTWindow currentWindow = wnd;
		if (currentWindow != null)
			currentWindow.setKeyboardLayer(keyboardLayer);
	}

	@Override
	public JOGLRenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(final String name) throws IOException {
		for (final Entry<String, JOGLFont> entry : fontCache.entrySet())
			if (entry.getKey().equals(name)) {
				rememberKeyboardFont(name, entry.getValue());
				return entry.getValue();
			}
		final JOGLFont font = new JOGLFont(name);
		this.registerFont(font);
		fontCache.put(name, font);
		rememberKeyboardFont(name, font);
		return font;
	}

	@Override
	public BinaryFont loadFont(final String path, final String name) throws IOException {
		for (final Entry<String, JOGLFont> entry : fontCache.entrySet())
			if (entry.getKey().equals(name)) {
				rememberKeyboardFont(name, entry.getValue());
				return entry.getValue();
			}
		final JOGLFont font = new JOGLFont(path, name);
		this.registerFont(font);
		fontCache.put(name, font);
		rememberKeyboardFont(name, font);
		return font;
	}

	private void rememberKeyboardFont(final String name, final JOGLFont font) {
		if ("smallest".equals(name))
			keyboardFont = font;
	}

	@Override
	public Skin loadSkin(final String file) throws IOException {
		return new JOGLSkin(file);
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isGPUEngineForced() == false)
			return false;
		boolean available = false;
		boolean errored = false;
		try {
			available = GLProfile.isAvailable(GLProfile.GL2ES2);
		} catch (final Exception ex) {
			errored = true;
			System.err.println("OpenGL Error: " + ex.getMessage());
		}
		if (!available && !errored)
			System.err.println(GLProfile.glAvailabilityToString());
		return available;
	}

	@Override
	public boolean doesRefreshPauses() {
		return false;
	}

	private void registerFont(final BinaryFont gpuFont) {
		if (gpuFont instanceof JOGLFont || gpuFont == null) {
			registeredFonts.add(gpuFont);
		} else {
			throw new IllegalArgumentException("Can't handle font type " + gpuFont.getClass().getSimpleName());
		}
	}

	@Override
	public boolean supportsFontRegistering() {
		return true;
	}

	@Override
	public CopyOnWriteArrayList<BinaryFont> getRegisteredFonts() {
		return registeredFonts;
	}

	public void registerTexture(final Texture t) {
		unregisteredTextures.addLast(t);
	}
}
