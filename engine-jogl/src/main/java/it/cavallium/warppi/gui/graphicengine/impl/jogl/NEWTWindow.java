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

import com.jogamp.nativewindow.NativeWindowFactory;
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
	private static final String DISABLE_LINUX_KEY_TRACKER = "newt.disable.LinuxKeyEventTracker";
	private static final String DISABLE_LINUX_MOUSE_TRACKER = "newt.disable.LinuxMouseTracker";

	private final JOGLEngine engine;
	private final JOGLRenderer renderer;
	private final JOGLKeyboardPanel keyboardPanel;
	public GLWindow window;
	public volatile float windowZoom = 1;
	public int[] realWindowSize;
	private volatile int displaySurfaceHeight;
	private volatile int keyboardSurfaceHeight;
	public Runnable onInitialized;
	public volatile boolean refreshViewport;

	final EventSubmitter<Integer[]> onRealResize;
	final EventSubmitter<Integer[]> onResizeEvent = EventSubmitter.create();
	private final EventSubmitter<Float> onZoom = EventSubmitter.create();
	private final EventSubmitter<GL2ES1> onGLContext = EventSubmitter.create();

	public NEWTWindow(final JOGLEngine engine) {
		this.engine = engine;
		renderer = engine.getRenderer();
		keyboardPanel = engine.isRaspberryProductionMode()
				? null : new JOGLKeyboardPanel(engine.getKeyboardLayer());
		engine.size[0] = engine.getSize()[0];
		engine.size[1] = engine.getSize()[1];
		windowZoom = StaticVars.windowZoom.getLastValue();
		if (windowZoom <= 0)
			windowZoom = 1;
		final int initialWidth = Math.max(1, Math.round(engine.getSize()[0] * windowZoom));
		final int initialHeight = Math.max(1, Math.round(engine.getSize()[1] * windowZoom))
				+ (keyboardPanel == null ? 0 : JOGLKeyboardPanel.LOGICAL_HEIGHT);
		realWindowSize = new int[] { initialWidth, initialHeight };
		updateSurfaceLayout();
		onRealResize = EventSubmitter.create(new Integer[] { initialWidth, initialHeight });

		onRealResize.subscribe((realSize) -> {
			realWindowSize[0] = Math.max(1, realSize[0]);
			realWindowSize[1] = Math.max(1, realSize[1]);
			updateSurfaceLayout();
			onResizeEvent.submit(new Integer[] { engine.size[0], engine.size[1] });
			refreshViewport = true;
		});
		StaticVars.windowZoom.subscribe(onZoom::submit);
		onZoom.subscribe((z) -> {
			if (z <= 0)
				return;
			windowZoom = z;
			updateSurfaceLayout();
			onResizeEvent.submit(new Integer[] { engine.size[0], engine.size[1] });
			refreshViewport = true;
		});
	}

	private void updateSurfaceLayout() {
		keyboardSurfaceHeight = keyboardPanel == null ? 0
				: JOGLKeyboardPanel.surfaceHeight(realWindowSize[1]);
		displaySurfaceHeight = Math.max(1, realWindowSize[1] - keyboardSurfaceHeight);
		engine.size[0] = Math.max(1, (int) (realWindowSize[0] / windowZoom));
		engine.size[1] = Math.max(1, (int) (displaySurfaceHeight / windowZoom));
	}

	public void create() {
		configureNativeWindowing();
		System.out.println("Loading OpenGL...");
		System.out.println(GLProfile.glAvailabilityToString());
		if (!GLProfile.isAvailable(GLProfile.GL2ES1)) {
			System.err.println("Le OpenGL non sono presenti su questo computer!");
			return;
		}
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
		final NEWTKeyboardInput keyboardInput = new NEWTKeyboardInput();

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
		final Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}

	private void pressKeyboardPanel(final MouseEvent event) {
		if (keyboardSurfaceHeight <= 0)
			return;
		final int windowWidth = Math.max(1, window.getWidth());
		final int windowHeight = Math.max(1, window.getHeight());
		final float surfaceX = event.getX() * (float) realWindowSize[0] / windowWidth;
		final float surfaceY = event.getY() * (float) realWindowSize[1] / windowHeight;
		if (surfaceY < displaySurfaceHeight)
			return;
		final float panelY = (surfaceY - displaySurfaceHeight)
				* JOGLKeyboardPanel.LOGICAL_HEIGHT / keyboardSurfaceHeight;
		keyboardPanel.press(surfaceX, panelY, realWindowSize[0]);
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

		// These must be set before NativeWindowFactory or GLProfile initializes.
		System.setProperty("java.awt.headless", "true");
		System.setProperty("newt.disable.PointerIcon", "true");
		// The appliance kernel has no Linux input subsystem. Its keyboard uses the GPIO scanner.
		// Keep JOGL from starting pollers for /dev/input devices that cannot exist.
		System.setProperty(DISABLE_LINUX_KEY_TRACKER, "true");
		System.setProperty(DISABLE_LINUX_MOUSE_TRACKER, "true");
		System.setProperty("nativewindow.ws.name", RASPBERRY_NATIVE_WINDOW_TYPE);

		final String detectedType = NativeWindowFactory.getNativeWindowType(false);
		final String selectedType = NativeWindowFactory.getNativeWindowType(true);
		if (!RASPBERRY_NATIVE_WINDOW_TYPE.equals(detectedType)
				|| !RASPBERRY_NATIVE_WINDOW_TYPE.equals(selectedType)) {
			throw new IllegalStateException("Refusing non-native Raspberry NEWT backend: detected="
					+ detectedType + ", selected=" + selectedType);
		}
		System.out.println("NEWT native backend: " + selectedType);
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

		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
	}

	@Override
	public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
		onRealResize.submit(new Integer[] { width, height });
	}

	@Override
	public void display(final GLAutoDrawable glad) {
		final GL2ES1 gl = glad.getGL().getGL2ES1();
		JOGLRenderer.gl = gl;
		onGLContext.submit(gl);

		final boolean linear = Math.abs(windowZoom - Math.round(windowZoom)) > 0.001f;
		final boolean viewportChanged = refreshViewport;
		if (keyboardPanel != null) {
			configureViewport(gl, 0, keyboardSurfaceHeight, realWindowSize[0], displaySurfaceHeight,
					engine.size[0], engine.size[1]);
		} else if (viewportChanged) {
			configureViewport(gl, 0, 0, realWindowSize[0], realWindowSize[1],
					engine.size[0], engine.size[1]);
		}
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

		gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);

		renderer.initDrawCycle();

		engine.repaint();

		renderer.endDrawCycle();
		if (keyboardPanel != null && keyboardSurfaceHeight > 0) {
			configureViewport(gl, 0, 0, realWindowSize[0], keyboardSurfaceHeight,
					realWindowSize[0], JOGLKeyboardPanel.LOGICAL_HEIGHT);
			keyboardPanel.render(renderer, realWindowSize[0], engine.getKeyboardFont());
		}

		gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);

		JOGLRenderer.gl = null;

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
		final int realWidth = Math.max(1, Math.round(width * windowZoom));
		final int realHeight = Math.max(1, Math.round(height * windowZoom))
				+ (keyboardPanel == null ? 0 : JOGLKeyboardPanel.LOGICAL_HEIGHT);
		window.setSize(realWidth, realHeight);
		onRealResize.submit(new Integer[] { realWidth, realHeight });
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		releaseKeyboardPanel();
		System.out.println("cleanup");
//		final GL2ES1 gl = drawable.getGL().getGL2ES1();
	}

}
