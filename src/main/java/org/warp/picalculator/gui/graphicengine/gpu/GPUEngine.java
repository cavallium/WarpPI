package org.warp.picalculator.gui.graphicengine.gpu;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import org.warp.picalculator.StaticVars;
import org.warp.picalculator.Utils;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.Skin;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;

import io.reactivex.Observable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.BehaviorSubject;

public class GPUEngine implements GraphicEngine {

	private volatile boolean initialized = false;
	private volatile boolean created = false;
	private NEWTWindow wnd;
	private RenderingLoop d;
	private GPURenderer r;
	private final Map<String, GPUFont> fontCache = new HashMap<>();
	int[] size = new int[] { StaticVars.screenSize[0], StaticVars.screenSize[1] };
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
	public void setTitle(String title) {
		wnd.window.setTitle(title);
	}

	@Override
	public void setResizable(boolean r) {
		if (!r) {
			wnd.window.setPosition(0, 0);
		}
		wnd.window.setResizable(r);
		wnd.window.setUndecorated(!r);
		wnd.window.setPointerVisible(r);
	}

	@Override
	public void setDisplayMode(int ww, int wh) {
		size[0] = ww;
		size[1] = wh;
		wnd.setSize(ww, wh);
	}

	@Override
	public void create() {
		create(null);
	}

	@Override
	public void create(Runnable onInitialized) {
		created = true;
		registeredTextures = new LinkedList<>();
		unregisteredTextures = new LinkedList<>();
		r = new GPURenderer();
		wnd = new NEWTWindow(this);
		wnd.create();
		setDisplayMode(StaticVars.screenSize[0], StaticVars.screenSize[1]);
		setResizable(StaticVars.debugOn & !Utils.debugThirdScreen);
		initialized = true;
		wnd.onInitialized = onInitialized;
	}
	
	@Override
	public Observable<Integer[]> onResize() {
		return wnd.onResize;
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
	public void start(RenderingLoop d) {
		this.d = d;
		wnd.window.setVisible(true);
	}

	@Override
	public void repaint() {
		if (d != null & r != null && GPURenderer.gl != null) {
			d.refresh();
		}
	}

	@Override
	public GPURenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(String name) throws IOException {
		for (final Entry<String, GPUFont> entry : fontCache.entrySet()) {
			if (entry.getKey().equals(name)) {
				return entry.getValue();
			}
		}
		final GPUFont font = new GPUFont(this, name);
		fontCache.put(name, font);
		return font;
	}

	@Override
	public BinaryFont loadFont(String path, String name) throws IOException {
		for (final Entry<String, GPUFont> entry : fontCache.entrySet()) {
			if (entry.getKey().equals(name)) {
				return entry.getValue();
			}
		}
		final GPUFont font = new GPUFont(this, path, name);
		fontCache.put(name, font);
		return font;
	}

	@Override
	public Skin loadSkin(String file) throws IOException {
		return new GPUSkin(this, file);
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isGPUEngineForced() == false) {
			return false;
		}
		if (StaticVars.startupArguments.isHeadlessEngineForced()) {
			return false;
		}
		boolean available = false;
		boolean errored = false;
		try {
			available = GLProfile.isAvailable(GLProfile.GL2ES2);
		} catch (final Exception ex) {
			errored = true;
			System.err.println("OpenGL Error: " + ex.getMessage());
		}
		if (!available && !errored) {
			System.err.println(GLProfile.glAvailabilityToString());
		}
		return available;
	}

	@Override
	public boolean doesRefreshPauses() {
		return false;
	}

	public void registerFont(GPUFont gpuFont) {
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

	public void registerTexture(Texture t) {
		unregisteredTextures.addLast(t);
	}

}