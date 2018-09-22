package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class JOGLEngine implements GraphicEngine {

	private volatile boolean initialized;
	private volatile boolean created;
	private NEWTWindow wnd;
	private RenderingLoop d;
	private JOGLRenderer r;
	private final Map<String, JOGLFont> fontCache = new HashMap<>();
	int[] size;
	private final CopyOnWriteArrayList<BinaryFont> registeredFonts = new CopyOnWriteArrayList<>();
	private final Semaphore exitSemaphore = new Semaphore(0);
	protected LinkedList<Texture> registeredTextures;
	protected LinkedList<Texture> unregisteredTextures;

	@Override
	public int[] getSize() {
		return size;
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
		wnd.window.setPointerVisible(r);
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
		size = new int[] { StaticVars.screenSize[0], StaticVars.screenSize[1] };
		created = true;
		registeredTextures = new LinkedList<>();
		unregisteredTextures = new LinkedList<>();
		r = new JOGLRenderer();
		wnd = new NEWTWindow(this);
		wnd.create();
		setDisplayMode(StaticVars.screenSize[0], StaticVars.screenSize[1]);
		setResizable(Engine.getPlatform().getSettings().isDebugEnabled());
		initialized = true;
		wnd.onInitialized = onInitialized;
	}

	@Override
	public Observable<Integer[]> onResize() {
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
	}

	@Override
	public void repaint() {
		if (d != null & r != null && JOGLRenderer.gl != null)
			d.refresh();
	}

	@Override
	public JOGLRenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(final String name) throws IOException {
		for (final Entry<String, JOGLFont> entry : fontCache.entrySet())
			if (entry.getKey().equals(name))
				return entry.getValue();
		final JOGLFont font = new JOGLFont(this, name);
		fontCache.put(name, font);
		return font;
	}

	@Override
	public BinaryFont loadFont(final String path, final String name) throws IOException {
		for (final Entry<String, JOGLFont> entry : fontCache.entrySet())
			if (entry.getKey().equals(name))
				return entry.getValue();
		final JOGLFont font = new JOGLFont(this, path, name);
		fontCache.put(name, font);
		return font;
	}

	@Override
	public Skin loadSkin(final String file) throws IOException {
		return new JOGLSkin(this, file);
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isGPUEngineForced() == false)
			return false;
		if (StaticVars.startupArguments.isHeadlessEngineForced())
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

	public void registerFont(final JOGLFont gpuFont) {
		registeredFonts.add(gpuFont);
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