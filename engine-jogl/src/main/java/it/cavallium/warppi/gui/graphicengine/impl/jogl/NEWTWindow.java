/**
 * Copyright 2012-2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.util.Properties;

import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.util.EventSubmitter;

/**
 *
 * @author Xerxes Rånby (xranby)
 * @author Andrea Cavalli (@Cavallium)
 */

class NEWTWindow implements GLEventListener {
	private static final String RASPBERRY_NATIVE_WINDOW_TYPE = ".bcm.vc.iv";
	private static final String DISABLE_OPENGL_DESKTOP = "jogl.disable.opengldesktop";
	private static final String DISABLE_LINUX_KEY_TRACKER = "newt.disable.LinuxKeyEventTracker";
	private static final String DISABLE_LINUX_MOUSE_TRACKER = "newt.disable.LinuxMouseTracker";
	private static final String OPENGL_DIAGNOSTICS = "warppi.jogl.diagnostics";
	private static final float SCALE_EPSILON = 0.001f;

	private final JOGLEngine engine;
	private final JOGLRenderer renderer;
	private final JOGLKeyboardPanel keyboardPanel;
	private final float desktopScale;
	public GLWindow window;
	private volatile float calculatorZoom = 1;
	private volatile float surfaceScaleX = ScalableSurface.IDENTITY_PIXELSCALE;
	private volatile float surfaceScaleY = ScalableSurface.IDENTITY_PIXELSCALE;
	private volatile int windowLogicalWidth;
	private volatile int windowLogicalHeight;
	private volatile SurfaceLayout surfaceLayout;
	private Animator animator;
	public Runnable onInitialized;
	public volatile boolean refreshViewport;

	final EventSubmitter<Integer[]> onRealResize;
	final EventSubmitter<Integer[]> onResizeEvent = EventSubmitter.create();
	private final EventSubmitter<Float> onZoom = EventSubmitter.create();
	private final EventSubmitter<GL2ES1> onGLContext = EventSubmitter.create();

	public NEWTWindow(final JOGLEngine engine) {
		this.engine = engine;
		renderer = engine.getRenderer();
		desktopScale = engine.getDesktopScale();
		keyboardPanel = engine.isRaspberryProductionMode()
				? null : new JOGLKeyboardPanel(engine.getKeyboardLayer());
		engine.size[0] = engine.getSize()[0];
		engine.size[1] = engine.getSize()[1];
		windowLogicalWidth = engine.size[0];
		windowLogicalHeight = engine.size[1];
		calculatorZoom = normalizedScale(StaticVars.windowZoom.getLastValue());
		final WindowUnits initialWindowSize = windowUnitsFor(engine.getSize()[0], engine.getSize()[1],
				windowCompensation(desktopScale, surfaceScaleX),
				windowCompensation(desktopScale, surfaceScaleY), keyboardPanel != null);
		final int initialSurfaceWidth = Math.max(1, Math.round(initialWindowSize.width() * surfaceScaleX));
		final int initialSurfaceHeight = Math.max(1, Math.round(initialWindowSize.height() * surfaceScaleY));
		surfaceLayout = calculateSurfaceLayout(initialSurfaceWidth, initialSurfaceHeight,
				calculatorZoom, resolvedDesktopScaleX(), resolvedDesktopScaleY(), keyboardPanel != null);
		onRealResize = EventSubmitter.create(new Integer[] { initialSurfaceWidth, initialSurfaceHeight });

		onRealResize.subscribe((surfaceSize) -> applySurfaceLayout(surfaceSize[0], surfaceSize[1]));
		StaticVars.windowZoom.subscribe(onZoom::submit);
		onZoom.subscribe((z) -> {
			if (z == null || !Float.isFinite(z) || z <= 0 || Math.abs(z - calculatorZoom) < SCALE_EPSILON)
				return;
			calculatorZoom = z;
			final SurfaceLayout currentLayout = surfaceLayout;
			if (currentLayout != null)
				applySurfaceLayout(currentLayout.surfaceWidth(), currentLayout.surfaceHeight());
		});
	}

	private void applySurfaceLayout(final int surfaceWidth, final int surfaceHeight) {
		final SurfaceLayout layout = calculateSurfaceLayout(surfaceWidth, surfaceHeight,
				calculatorZoom, resolvedDesktopScaleX(), resolvedDesktopScaleY(), keyboardPanel != null);
		surfaceLayout = layout;
		engine.size[0] = layout.logicalWidth();
		engine.size[1] = layout.logicalHeight();
		windowLogicalWidth = layout.windowLogicalWidth();
		windowLogicalHeight = layout.windowLogicalHeight();
		onResizeEvent.submit(new Integer[] { layout.logicalWidth(), layout.logicalHeight() });
		refreshViewport = true;
	}

	private boolean refreshSurfaceScale() {
		final GLWindow currentWindow = window;
		if (currentWindow == null)
			return false;
		final float[] currentScale = currentWindow.getCurrentSurfaceScale(new float[2]);
		final float newScaleX = normalizedScale(currentScale[0]);
		final float newScaleY = normalizedScale(currentScale[1]);
		if (Math.abs(newScaleX - surfaceScaleX) < SCALE_EPSILON
				&& Math.abs(newScaleY - surfaceScaleY) < SCALE_EPSILON)
			return false;
		surfaceScaleX = newScaleX;
		surfaceScaleY = newScaleY;
		return true;
	}

	private float resolvedDesktopScaleX() {
		return Math.max(desktopScale, surfaceScaleX);
	}

	private float resolvedDesktopScaleY() {
		return Math.max(desktopScale, surfaceScaleY);
	}

	private boolean resizeWindow(final int logicalWidth, final int logicalHeight) {
		windowLogicalWidth = Math.max(1, logicalWidth);
		windowLogicalHeight = Math.max(1, logicalHeight);
		final GLWindow currentWindow = window;
		if (currentWindow == null)
			return false;
		final WindowUnits target = windowUnitsFor(windowLogicalWidth, windowLogicalHeight,
				windowCompensation(desktopScale, surfaceScaleX),
				windowCompensation(desktopScale, surfaceScaleY), keyboardPanel != null);
		if (currentWindow.getWidth() == target.width() && currentWindow.getHeight() == target.height())
			return false;
		currentWindow.setSize(target.width(), target.height());
		return true;
	}

	private static float normalizedScale(final Float scale) {
		return scale != null && Float.isFinite(scale) && scale > 0
				? scale : ScalableSurface.IDENTITY_PIXELSCALE;
	}

	static WindowUnits windowUnitsFor(final int logicalWidth, final int logicalHeight,
			final float surfaceCompensationX, final float surfaceCompensationY,
			final boolean hasKeyboard) {
		final float checkedCompensationX = requireScale(surfaceCompensationX, "surface compensation X");
		final float checkedCompensationY = requireScale(surfaceCompensationY, "surface compensation Y");
		final int keyboardHeight = hasKeyboard ? JOGLKeyboardPanel.LOGICAL_HEIGHT : 0;
		return new WindowUnits(
				Math.max(1, Math.round(Math.max(1, logicalWidth) * checkedCompensationX)),
				Math.max(1, Math.round((Math.max(1, logicalHeight) + keyboardHeight)
						* checkedCompensationY)));
	}

	static float windowCompensation(final float desktopScale, final float surfaceScale) {
		final float checkedSurfaceScale = requireScale(surfaceScale, "JOGL surface scale");
		return Math.max(requireScale(desktopScale, "desktop scale"), checkedSurfaceScale)
				/ checkedSurfaceScale;
	}

	static SurfaceLayout calculateSurfaceLayout(final int surfaceWidth, final int surfaceHeight,
			final float calculatorZoom, final float deviceScaleX, final float deviceScaleY,
			final boolean hasKeyboard) {
		final int checkedSurfaceWidth = Math.max(1, surfaceWidth);
		final int checkedSurfaceHeight = Math.max(1, surfaceHeight);
		final float checkedCalculatorZoom = requireScale(calculatorZoom, "calculator zoom");
		final float checkedDeviceScaleX = requireScale(deviceScaleX, "device scale X");
		final float checkedDeviceScaleY = requireScale(deviceScaleY, "device scale Y");
		final float effectiveScaleX = checkedCalculatorZoom * checkedDeviceScaleX;
		final float effectiveScaleY = checkedCalculatorZoom * checkedDeviceScaleY;
		if (!Float.isFinite(effectiveScaleX) || !Float.isFinite(effectiveScaleY))
			throw new IllegalArgumentException("Combined pixel scale is too large");
		final int keyboardHeight = hasKeyboard
				? JOGLKeyboardPanel.surfaceHeight(checkedSurfaceHeight, checkedDeviceScaleY) : 0;
		final int displayHeight = Math.max(1, checkedSurfaceHeight - keyboardHeight);
		return new SurfaceLayout(checkedSurfaceWidth, checkedSurfaceHeight, displayHeight, keyboardHeight,
				Math.max(1, Math.round(checkedSurfaceWidth / checkedDeviceScaleX)),
				Math.max(1, Math.round(displayHeight / checkedDeviceScaleY)),
				Math.max(1, Math.round(checkedSurfaceWidth / effectiveScaleX)),
				Math.max(1, Math.round(displayHeight / effectiveScaleY)));
	}

	private static float requireScale(final float scale, final String name) {
		if (!Float.isFinite(scale) || scale <= 0)
			throw new IllegalArgumentException("Invalid " + name + ": " + scale);
		return scale;
	}

	record WindowUnits(int width, int height) {}

	record SurfaceLayout(int surfaceWidth, int surfaceHeight, int displaySurfaceHeight,
			int keyboardSurfaceHeight, int windowLogicalWidth, int windowLogicalHeight,
			int logicalWidth, int logicalHeight) {}

	public void create() {
		configureNativeWindowing();
		System.out.println("Loading OpenGL...");
		if (openGLDiagnosticsEnabled())
			System.out.println(GLProfile.glAvailabilityToString());
		if (WarpPI.getPlatform().getSettings().isDebugEnabled() && !engine.isRaspberryProductionMode())
			System.setProperty("jnlp.newt.window.icons", "res/icons/calculator-016.png res/icons/calculator-018.png res/icons/calculator-256.png");
		final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES1));
		System.out.println("Loaded OpenGL");
		// We may at this point tweak the caps and request a translucent drawable
		caps.setHardwareAccelerated(true);
		caps.setRedBits(5);
		caps.setGreenBits(6);
		caps.setBlueBits(5);
		caps.setAlphaBits(0);
		caps.setBackgroundOpaque(true); //transparency window
//		caps.setSampleBuffers(true);
//		caps.setNumSamples(4);

		final GLWindow glWindow = GLWindow.create(caps);
		window = glWindow;
		glWindow.setSurfaceScale(new float[] {
				ScalableSurface.AUTOMAX_PIXELSCALE, ScalableSurface.AUTOMAX_PIXELSCALE });
		refreshSurfaceScale();
		final NEWTKeyboardInput keyboardInput = engine.isRaspberryProductionMode()
				? null : new NEWTKeyboardInput();

		glWindow.setTitle("WarpPI Calculator by Andrea Cavalli (@Cavallium)");

		glWindow.addWindowListener(new WindowListener() {

			@Override
			public void windowDestroyNotify(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDestroyed(final WindowEvent e) {
				releaseKeyboardPanel();
				if (engine.isInitialized())
					engine.destroy();
			}

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowLostFocus(final WindowEvent e) {
				if (keyboardInput != null)
					keyboardInput.releaseAll();
				releaseKeyboardPanel();
			}

			@Override
			public void windowMoved(final WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowRepaint(final WindowUpdateEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowResized(final WindowEvent e) {

			}

		});
		if (keyboardInput != null)
			glWindow.addKeyListener(keyboardInput);
		if (keyboardPanel != null) {
			glWindow.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent event) {
					if (event.getButton() == MouseEvent.BUTTON1)
						pressKeyboardPanel(event);
				}

				@Override
				public void mouseReleased(final MouseEvent event) {
					if (event.getButton() == MouseEvent.BUTTON1)
						releaseKeyboardPanel();
				}

				@Override
				public void mouseExited(final MouseEvent event) {
					releaseKeyboardPanel();
				}
			});
		}

		glWindow.addGLEventListener(this /* GLEventListener */);
		animator = new Animator();
		animator.add(glWindow);
	}

	void startAnimator() {
		final Animator currentAnimator = animator;
		if (currentAnimator == null)
			throw new IllegalStateException("The NEWT window has not been created");
		if (!currentAnimator.isStarted() && !currentAnimator.start())
			throw new IllegalStateException("The NEWT animator did not start");
	}

	private void pressKeyboardPanel(final MouseEvent event) {
		final SurfaceLayout layout = surfaceLayout;
		if (layout == null || layout.keyboardSurfaceHeight() <= 0)
			return;
		final int windowWidth = Math.max(1, window.getWidth());
		final int windowHeight = Math.max(1, window.getHeight());
		final float surfaceX = event.getX() * (float) layout.surfaceWidth() / windowWidth;
		final float surfaceY = event.getY() * (float) layout.surfaceHeight() / windowHeight;
		if (surfaceY < layout.displaySurfaceHeight())
			return;
		final float panelX = surfaceX * layout.windowLogicalWidth() / layout.surfaceWidth();
		final float panelY = (surfaceY - layout.displaySurfaceHeight())
				* JOGLKeyboardPanel.LOGICAL_HEIGHT / layout.keyboardSurfaceHeight();
		keyboardPanel.press(panelX, panelY, layout.windowLogicalWidth());
	}

	private void releaseKeyboardPanel() {
		if (keyboardPanel != null)
			keyboardPanel.release();
	}

	void setKeyboardLayer(final Layer layer) {
		if (keyboardPanel != null)
			keyboardPanel.setLayer(layer);
	}

	private void configureNativeWindowing() {
		if (!engine.isRaspberryProductionMode())
			return;

		final String detectedType = NativeWindowFactory.getNativeWindowType(false);
		final String selectedType = NativeWindowFactory.getNativeWindowType(true);
		if (!RASPBERRY_NATIVE_WINDOW_TYPE.equals(detectedType)
				|| !RASPBERRY_NATIVE_WINDOW_TYPE.equals(selectedType)) {
			throw new IllegalStateException("Refusing non-native Raspberry NEWT backend: detected="
					+ detectedType + ", selected=" + selectedType);
		}
		System.out.println("NEWT native backend: " + selectedType);
	}

	static void configureRaspberrySystemProperties(final Properties properties) {
		// These must be set before NativeWindowFactory or GLProfile initializes.
		properties.setProperty("java.awt.headless", "true");
		properties.setProperty("newt.disable.PointerIcon", "true");
		// The appliance kernel has no Linux input subsystem. Its keyboard uses the GPIO scanner.
		// Keep JOGL from starting pollers for /dev/input devices that cannot exist.
		properties.setProperty(DISABLE_LINUX_KEY_TRACKER, "true");
		properties.setProperty(DISABLE_LINUX_MOUSE_TRACKER, "true");
		properties.setProperty("nativewindow.ws.name", RASPBERRY_NATIVE_WINDOW_TYPE);
		// The appliance uses Broadcom EGL/OpenGL ES 1; probing desktop GL only wastes startup time.
		properties.setProperty(DISABLE_OPENGL_DESKTOP, "true");
	}

	private boolean openGLDiagnosticsEnabled() {
		return openGLDiagnosticsEnabled(engine.isRaspberryProductionMode(), System.getProperties());
	}

	static boolean openGLDiagnosticsEnabled(final boolean raspberryProductionMode,
			final Properties properties) {
		return !raspberryProductionMode
				|| Boolean.parseBoolean(properties.getProperty(OPENGL_DIAGNOSTICS));
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		final GL2ES1 gl = drawable.getGL().getGL2ES1();
		onGLContext.submit(gl);

		if (WarpPI.getPlatform().getSettings().isDebugEnabled())
			//Vsync
			gl.setSwapInterval(1);
		else
			//Vsync
			gl.setSwapInterval(2);

		//Textures
		gl.glEnable(GL.GL_TEXTURE_2D);

		//Transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glShadeModel(GLLightingFunc.GL_FLAT);

		//Multisampling
		//gl.glEnable(GL.GL_MULTISAMPLE);

		if (onInitialized != null) {
			onInitialized.run();
			onInitialized = null;
		}

		if (openGLDiagnosticsEnabled()) {
			System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
			System.err.println("INIT GL IS: " + gl.getClass().getName());
			System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
			System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
			System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		}
	}

	@Override
	public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
		if (refreshSurfaceScale() && resizeWindow(windowLogicalWidth, windowLogicalHeight))
			return;
		onRealResize.submit(new Integer[] {
				Math.max(1, glad.getSurfaceWidth()), Math.max(1, glad.getSurfaceHeight()) });
	}

	@Override
	public void display(final GLAutoDrawable glad) {
		final GL2ES1 gl = glad.getGL().getGL2ES1();
		JOGLRenderer.gl = gl;
		onGLContext.submit(gl);
		engine.destroyReleasedTextures(gl);

		if (refreshSurfaceScale()) {
			if (resizeWindow(windowLogicalWidth, windowLogicalHeight)) {
				JOGLRenderer.gl = null;
				return;
			}
			applySurfaceLayout(glad.getSurfaceWidth(), glad.getSurfaceHeight());
		}
		final SurfaceLayout layout = surfaceLayout;
		if (layout == null)
			return;
		final float effectiveScaleX = calculatorZoom * resolvedDesktopScaleX();
		final float effectiveScaleY = calculatorZoom * resolvedDesktopScaleY();
		final boolean linear = isFractional(effectiveScaleX) || isFractional(effectiveScaleY);
		final boolean viewportChanged = refreshViewport;
		if (keyboardPanel != null) {
			configureViewport(gl, 0, layout.keyboardSurfaceHeight(),
					layout.surfaceWidth(), layout.displaySurfaceHeight(),
					layout.logicalWidth(), layout.logicalHeight());
		} else if (viewportChanged) {
			configureViewport(gl, 0, 0, layout.surfaceWidth(), layout.surfaceHeight(),
					layout.logicalWidth(), layout.logicalHeight());
		}
		synchronized (engine) {
			if (viewportChanged) {
				refreshViewport = false;
				for (final Texture t : engine.registeredTextures) {
					t.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, linear ? GL.GL_LINEAR : GL.GL_NEAREST);
					t.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				}
			}
			while (engine.unregisteredTextures.isEmpty() == false) {
				final Texture t = engine.unregisteredTextures.pop();
				t.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, linear ? GL.GL_LINEAR : GL.GL_NEAREST);
				t.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				engine.registeredTextures.addLast(t);
			}
		}

		gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);

		renderer.beginFrameDiagnostics();
		renderer.initDrawCycle();

		engine.repaint();

		renderer.endDrawCycle();
		engine.destroyReleasedTextures(gl);
		if (keyboardPanel != null && layout.keyboardSurfaceHeight() > 0) {
			configureViewport(gl, 0, 0, layout.surfaceWidth(), layout.keyboardSurfaceHeight(),
					layout.windowLogicalWidth(), JOGLKeyboardPanel.LOGICAL_HEIGHT);
			keyboardPanel.render(renderer, layout.windowLogicalWidth(), engine.getKeyboardFont());
		}
		renderer.endFrameDiagnostics();

		gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);

		JOGLRenderer.gl = null;

	}

	private static boolean isFractional(final float scale) {
		return Math.abs(scale - Math.round(scale)) > SCALE_EPSILON;
	}

	private static void configureViewport(final GL2ES1 gl, final int x, final int y,
			final int surfaceWidth, final int surfaceHeight,
			final int logicalWidth, final int logicalHeight) {
		gl.glViewport(x, y, Math.max(1, surfaceWidth), Math.max(1, surfaceHeight));
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0.0, Math.max(1, logicalWidth), Math.max(1, logicalHeight), 0.0, -1, 1);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	void setSize(final int width, final int height) {
		resizeWindow(width, height);
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		releaseKeyboardPanel();
		engine.destroyReleasedTextures(drawable.getGL());
		System.out.println("cleanup");
//		final GL2ES1 gl = drawable.getGL().getGL2ES1();
	}

}
